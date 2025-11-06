package com.qianyu.chatroom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qianyu.chatroom.entry.ReturnBody;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * JWT认证入口点
 * 处理未认证的请求，返回统一的错误响应
 */
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationEntryPoint.class);

    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) 
            throws IOException, ServletException {
        
        String requestPath = request.getRequestURI();
        String authHeader = request.getHeader("Authorization");
        
        logger.warn("未授权访问: {}, Authorization: {}", requestPath, 
                authHeader != null ? "已提供" : "未提供");
        
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        
        // 创建错误响应
        String errorMessage = "未授权，请先登录";
        if (authHeader == null || authHeader.trim().isEmpty()) {
            errorMessage = "未授权，请求头中缺少Authorization字段，请先登录";
        } else if (!authHeader.startsWith("Bearer ")) {
            errorMessage = "未授权，Authorization格式错误，正确格式为: Bearer <token>";
        }
        
        ReturnBody errorResponse = ReturnBody.Error("401", errorMessage);
        
        // 将响应转换为JSON并写入响应体
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
    }
}

