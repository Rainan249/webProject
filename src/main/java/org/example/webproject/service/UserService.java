package org.example.webproject.service;

import org.example.webproject.dto.LoginResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.*;
import java.util.HexFormat;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService {

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    /** PBKDF2 迭代次数与盐长度（远好于明文/裸哈希，零新增依赖） */
    private static final int PBKDF2_ITERATIONS = 32_768;
    private static final int SALT_BYTES = 16;
    private static final int KEY_BITS = 256;

    private final SecureRandom secureRandom = new SecureRandom();

    /** 内存会话：token -> username。重启后失效，需重新登录。 */
    private final Map<String, String> sessions = new ConcurrentHashMap<>();

    @Value("${database.url:jdbc:sqlite:register.db}")
    private String dbUrl;

    @PostConstruct
    public void init() {
        try (Connection conn = DriverManager.getConnection(dbUrl);
             Statement stmt = conn.createStatement()) {
            String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    password TEXT NOT NULL
                )
            """;
            stmt.executeUpdate(sql);

            // 无任何用户时创建默认账号，密码以 PBKDF2 哈希存储，不再明文
            ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM users");
            if (rs.next() && rs.getInt(1) == 0) {
                try (PreparedStatement insert = conn.prepareStatement(
                        "INSERT INTO users (username, password) VALUES (?, ?)")) {
                    insert.setString(1, "root");
                    insert.setString(2, hashPbkdf2("123456"));
                    insert.executeUpdate();
                }
                log.info("已创建默认用户: root/123456（密码已加密存储），请登录后修改密码");
            }
        } catch (SQLException e) {
            log.error("数据库初始化失败", e);
        }
    }

    public LoginResponse login(String username, String password) {
        if (username == null || username.trim().isEmpty() ||
            password == null || password.trim().isEmpty()) {
            return LoginResponse.failure("请输入账号和密码");
        }

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT * FROM users WHERE username = ?"
             )) {
            stmt.setString(1, username.trim());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                String stored = rs.getString("password");
                if (!verifyAndUpgrade(conn, rs.getLong("id"), stored, password.trim())) {
                    return LoginResponse.failure("账号或密码错误");
                }
                // 登录成功：签发随机会话 Token
                String token = UUID.randomUUID().toString().replace("-", "");
                sessions.put(token, username.trim());
                log.info("用户 {} 登录成功，已签发会话 Token", username.trim());
                return LoginResponse.success(username.trim(), token);
            }
            return LoginResponse.failure("账号或密码错误");
        } catch (SQLException e) {
            log.error("登录查询失败", e);
            return LoginResponse.failure("系统错误，请稍后重试");
        }
    }

    /** 校验 Token 是否有效 */
    public boolean isValidToken(String token) {
        return token != null && sessions.containsKey(token);
    }

    /** 注销会话 */
    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    /** 获取 Token 对应的用户名 */
    public String getUsernameByToken(String token) {
        return sessions.get(token);
    }

    /**
     * 修改密码：校验旧密码 → PBKDF2 存新密码 → 注销该用户所有旧会话（要求重新登录）。
     * @return null=成功；否则为错误信息
     */
    public String changePassword(String token, String oldPassword, String newPassword) {
        String username = getUsernameByToken(token);
        if (username == null) return "会话已过期，请重新登录";
        if (newPassword == null || newPassword.trim().length() < 6) {
            return "新密码长度至少 6 位";
        }
        if (newPassword.equals(oldPassword)) return "新密码不能与旧密码相同";

        try (Connection conn = DriverManager.getConnection(dbUrl);
             PreparedStatement stmt = conn.prepareStatement("SELECT * FROM users WHERE username = ?")) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            if (!rs.next()) return "用户不存在";

            long userId = rs.getLong("id");
            String stored = rs.getString("password");
            if (!verifyAndUpgrade(conn, userId, stored, oldPassword == null ? "" : oldPassword.trim())) {
                return "旧密码错误";
            }

            try (PreparedStatement up = conn.prepareStatement(
                    "UPDATE users SET password = ? WHERE id = ?")) {
                up.setString(1, hashPbkdf2(newPassword.trim()));
                up.setLong(2, userId);
                up.executeUpdate();
            }
            // 密码已变更：注销该用户全部旧会话
            sessions.values().removeIf(u -> u.equals(username));
            log.info("用户 {} 修改密码成功，已注销其全部会话", username);
            return null;
        } catch (SQLException e) {
            log.error("修改密码失败", e);
            return "系统错误，请稍后重试";
        }
    }

    /** 校验密码；若库里仍是存量明文且密码正确，则原地升级为 PBKDF2 哈希 */
    private boolean verifyAndUpgrade(Connection conn, long userId, String stored, String password) {
        try {
            if (stored != null && stored.startsWith("pbkdf2:")) {
                return verifyPbkdf2(stored, password);
            }
            // 兼容存量明文：密码正确则升级为加密存储
            if (stored != null && stored.equals(password)) {
                try (PreparedStatement up = conn.prepareStatement(
                        "UPDATE users SET password = ? WHERE id = ?")) {
                    up.setString(1, hashPbkdf2(password));
                    up.setLong(2, userId);
                    up.executeUpdate();
                    log.info("检测到存量明文密码，已自动升级为 PBKDF2 加密存储（userId={}）", userId);
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            log.error("密码校验/升级失败", e);
            return false;
        }
    }

    /** 生成 "pbkdf2:iterations:saltHex:hashHex" 格式的密码串 */
    private String hashPbkdf2(String rawPassword) {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        byte[] hash = pbkdf2(rawPassword, salt, PBKDF2_ITERATIONS);
        return "pbkdf2:%d:%s:%s".formatted(PBKDF2_ITERATIONS,
                HexFormat.of().formatHex(salt), HexFormat.of().formatHex(hash));
    }

    /** 按存储格式校验明文密码（迭代次数/盐取自存储串，便于将来调参兼容） */
    private boolean verifyPbkdf2(String stored, String rawPassword) {
        try {
            String[] parts = stored.split(":");
            if (parts.length != 4) return false;
            int iterations = Integer.parseInt(parts[1]);
            byte[] salt = HexFormat.of().parseHex(parts[2]);
            byte[] expected = HexFormat.of().parseHex(parts[3]);
            byte[] actual = pbkdf2(rawPassword, salt, iterations);
            return MessageDigest.isEqual(expected, actual);
        } catch (Exception e) {
            return false;
        }
    }

    private byte[] pbkdf2(String rawPassword, byte[] salt, int iterations) {
        try {
            PBEKeySpec spec = new PBEKeySpec(rawPassword.toCharArray(), salt, iterations, KEY_BITS);
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
                    .generateSecret(spec).getEncoded();
        } catch (Exception e) {
            throw new IllegalStateException("PBKDF2 计算失败", e);
        }
    }
}
