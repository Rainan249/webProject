package org.example.webproject.controller;

import org.example.webproject.dto.LoginRequest;
import org.example.webproject.dto.LoginResponse;
import org.example.webproject.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class LoginController {

    private final UserService userService;

    public LoginController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        return userService.login(request.getUsername(), request.getPassword());
    }

    /** 注销：销毁服务端会话 Token */
    @PostMapping("/logout")
    public java.util.Map<String, Object> logout(@RequestHeader(value = "X-Auth-Token", required = false) String token) {
        userService.logout(token);
        return java.util.Map.of("success", true, "message", "已退出登录");
    }

    /** 修改密码：校验旧密码，新密码 PBKDF2 加密存储，成功后需重新登录 */
    @PostMapping("/password")
    public java.util.Map<String, Object> changePassword(
            @RequestHeader(value = "X-Auth-Token", required = false) String token,
            @RequestBody java.util.Map<String, String> body) {
        String error = userService.changePassword(
                token, body.get("oldPassword"), body.get("newPassword"));
        if (error == null) {
            return java.util.Map.of("success", true, "message", "密码修改成功，请重新登录");
        }
        return java.util.Map.of("success", false, "message", error);
    }
}
