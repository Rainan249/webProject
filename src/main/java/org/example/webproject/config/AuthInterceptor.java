package org.example.webproject.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.example.webproject.service.UserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 会话鉴权拦截器：校验请求头 X-Auth-Token。
 * 放行：/api/login、/api/logout、/api/tmdb/**（在 WebConfig 中按路径注册，静态资源不走此拦截器）。
 */
@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String UNAUTHORIZED_JSON =
            "{\"success\":false,\"message\":\"未登录或会话已过期，请重新登录\"}";

    private final UserService userService;

    public AuthInterceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        // 放行预检（如将来跨域部署前端时需要）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        String token = request.getHeader("X-Auth-Token");
        if (userService.isValidToken(token)) {
            return true;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(UNAUTHORIZED_JSON);
        return false;
    }
}
