package com.qianyu.chatroom.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigInteger;

/**
 * SecurityContext工具类
 * 用于从SecurityContext中获取当前登录用户信息
 * 
 * 注意：Spring Security 的 SecurityContext 使用 ThreadLocal 存储，每个请求线程都有独立的上下文
 * 因此完全支持多用户并发登录，不会出现用户信息混淆的问题
 */
public class SecurityContextUtil {

    private static final Logger logger = LoggerFactory.getLogger(SecurityContextUtil.class);

    /**
     * 获取当前登录用户的ID（String类型）
     * JWT token中存储的是String类型，所以这是最直接的方式
     * @return 用户ID字符串，如果未登录则返回null
     */
    public static String getCurrentUserIdAsString() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof String) {
                return (String) principal;
            } else {
                logger.warn("Principal类型不匹配，期望String，实际: {}", principal.getClass().getName());
                return principal.toString();
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户的ID（BigInteger类型）
     * 用于数据库查询等需要BigInteger的场景
     * @return 用户ID（BigInteger），如果未登录或转换失败则返回null
     */
    public static BigInteger getCurrentUserId() {
        String userIdStr = getCurrentUserIdAsString();
        if (userIdStr != null && !userIdStr.isEmpty()) {
            try {
                return new BigInteger(userIdStr);
            } catch (NumberFormatException e) {
                logger.error("无法将用户ID转换为BigInteger: {}", userIdStr, e);
                return null;
            }
        }
        return null;
    }

    /**
     * 获取当前登录用户的用户名
     * 从JWT token的详细信息中获取
     * @return 用户名，如果未登录则返回null
     */
    public static String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getDetails() != null) {
            // 如果需要在token中存储更多信息，可以从这里获取
            // 目前用户名信息在token的claims中，可以通过JwtUtil获取
            return null;
        }
        return null;
    }

    /**
     * 检查当前用户是否已认证
     * @return 是否已认证
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null 
                && authentication.isAuthenticated() 
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    /**
     * 获取当前认证对象
     * @return Authentication对象
     */
    public static Authentication getAuthentication() {
        return SecurityContextHolder.getContext().getAuthentication();
    }

    /**
     * 清除当前线程的认证信息
     * 通常在登出或需要清除认证状态时使用
     */
    public static void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }
}

