package com.qianyu.chatroom.filter;

import com.qianyu.chatroom.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT认证过滤器
 * 在每个请求中验证Authorization请求头中的JWT token
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) 
            throws ServletException, IOException {
        
        String requestPath = request.getRequestURI();
        logger.debug("处理请求: {}", requestPath);
        
        // 获取Authorization请求头
        String token = getTokenFromRequest(request);
        
        if (StringUtils.hasText(token)) {
            logger.debug("找到Authorization请求头");
            
            // 验证token
            if (jwtUtil.validateToken(token)) {
                // 从token中获取用户信息
                String userId = jwtUtil.getUserIdFromToken(token);
                String username = jwtUtil.getUsernameFromToken(token);
                
                if (userId != null && username != null) {
                    logger.debug("Token验证成功，用户ID: {}, 用户名: {}", userId, username);
                    
                    // 创建认证对象
                    UsernamePasswordAuthenticationToken authentication = 
                        new UsernamePasswordAuthenticationToken(
                            userId, // principal - 用户ID
                            null,   // credentials - 不需要密码
                            Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                        );
                    
                    // 设置认证详情
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    
                    // 将认证信息存储到SecurityContext中
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                    logger.debug("用户认证信息已设置到SecurityContext");
                } else {
                    logger.warn("Token验证成功，但无法获取用户信息");
                }
            } else {
                logger.warn("Token验证失败: {}", requestPath);
            }
        } else {
            logger.debug("请求中没有Authorization请求头: {}", requestPath);
        }
        
        // 继续过滤器链
        filterChain.doFilter(request, response);
    }

    /**
     * 从请求头中提取token
     * 支持格式: "Authorization: Bearer <token>"
     * 也支持直接传递token（兼容性处理）
     * @param request HTTP请求
     * @return token字符串，如果不存在则返回null
     */
    private String getTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken)) {
            if (bearerToken.startsWith("Bearer ")) {
                return bearerToken.substring(7); // 移除"Bearer "前缀
            } else {
                // 如果没有Bearer前缀，直接返回（兼容性处理）
                return bearerToken;
            }
        }
        return null;
    }

}

