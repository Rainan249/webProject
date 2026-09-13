package org.example.webproject.dto;

import lombok.Data;

@Data
public class LoginResponse {
    private boolean success;
    private String message;
    private String username;
    /** 会话令牌：后续请求通过请求头 X-Auth-Token 携带 */
    private String token;

    public static LoginResponse success(String username) {
        return success(username, null);
    }

    public static LoginResponse success(String username, String token) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(true);
        response.setUsername(username);
        response.setToken(token);
        response.setMessage("登录成功");
        return response;
    }

    public static LoginResponse failure(String message) {
        LoginResponse response = new LoginResponse();
        response.setSuccess(false);
        response.setMessage(message);
        return response;
    }
}
