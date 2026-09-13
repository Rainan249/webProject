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
}
