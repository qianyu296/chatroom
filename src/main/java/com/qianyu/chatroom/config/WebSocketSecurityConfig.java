package com.qianyu.chatroom.config;

import com.qianyu.chatroom.manager.UserPresenceManager;
import com.qianyu.chatroom.mapper.FriendMapper;
import com.qianyu.chatroom.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket安全配置
 * 用于在WebSocket连接时验证JWT token
 * 同时处理用户上线/下线状态广播
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 99)
public class WebSocketSecurityConfig implements ChannelInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketSecurityConfig.class);

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private UserPresenceManager userPresenceManager;

    @Autowired
    @Lazy
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private FriendMapper friendMapper;

    // 使用 Map 跟踪 sessionId -> userId 的映射（支持多个连接）
    private final Map<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            logger.info("[WebSocketSecurity] 收到CONNECT请求");

            // 获取Authorization请求头
            List<String> authHeaders = accessor.getNativeHeader("Authorization");

            if (authHeaders == null || authHeaders.isEmpty()) {
                logger.error("[WebSocketSecurity] CONNECT缺少Authorization头");
                throw new IllegalStateException("Missing Authorization header");
            }

            String authHeader = authHeaders.get(0);
            String token = null;

            // 提取token
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                token = authHeader.substring(7);
            } else if (authHeader != null) {
                token = authHeader;
            }

            logger.info("[WebSocketSecurity] token长度: {}", token != null ? token.length() : 0);

            // 验证token
            if (token == null || !jwtUtil.validateToken(token)) {
                logger.error("[WebSocketSecurity] token验证失败");
                throw new IllegalStateException("Invalid or expired token");
            }

            String userId = jwtUtil.getUserIdFromToken(token);
            String username = jwtUtil.getUsernameFromToken(token);

            logger.info("[WebSocketSecurity] token验证成功, userId: {}, username: {}", userId, username);

            if (userId == null || username == null) {
                logger.error("[WebSocketSecurity] token payload无效");
                throw new IllegalStateException("Invalid token payload");
            }

            // 保存sessionId -> userId映射用于断开时使用
            String sessionId = accessor.getSessionId();
            sessionUserMap.put(sessionId, userId);
            logger.info("[WebSocketSecurity] 保存session映射: sessionId={}, userId={}", sessionId, userId);

            // 创建认证对象
            UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
                );

            // 设置认证信息
            accessor.setUser(authentication);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            logger.info("[WebSocketSecurity] 用户认证成功 : {}", userId);

            // 用户上线
            userPresenceManager.userOnline(userId);

            // 广播用户上线状态给所有好友
            broadcastPresenceChange(userId, "在线");

            // 向当前用户推送所有好友的当前在线状态
            pushFriendsPresenceToUser(userId);
        }

        return message;
    }

    /**
     * WebSocket会话断开事件处理
     */
    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        String sessionId = event.getSessionId();
        logger.info("[WebSocketSecurity] SessionDisconnectEvent, event.getUser(): {}", event.getUser());
        logger.info("[WebSocketSecurity] SessionDisconnectEvent, sessionId: {}", sessionId);

        // 从session映射中获取userId
        String userId = sessionUserMap.remove(sessionId);
        if (userId == null) {
            userId = event.getUser() != null ? event.getUser().getName() : null;
        }

        if (userId != null) {
            logger.info("[WebSocketSecurity] 用户断开连接: {}", userId);

            // 用户下线
            userPresenceManager.userOffline(userId);

            // 广播用户下线状态给所有好友
            broadcastPresenceChange(userId, "离线");
        }
    }

    /**
     * 向指定用户推送所有好友的当前在线状态
     */
    private void pushFriendsPresenceToUser(String userId) {
        new Thread(() -> {
            try {
                // 等待一小段时间确保WebSocket会话完全建立
                Thread.sleep(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            try {
                List<BigInteger> friendIds = friendMapper.selectFriendIdsByUserId(new BigInteger(userId));

                logger.info("[Presence] 向用户 {} 推送 {} 个好友的在线状态", userId, friendIds.size());
                logger.info("[Presence] onlineUsers 当前内容: {}", userPresenceManager.getAllOnlineUsers());

                for (BigInteger friendId : friendIds) {
                    String friendIdStr = friendId.toString();
                    boolean isOnline = userPresenceManager.isOnline(friendIdStr);
                    String status = isOnline ? "在线" : "离线";

                    logger.info("[Presence] 好友 {} 在线状态检查: isOnline={}", friendIdStr, isOnline);

                    Map<String, Object> presenceData = new HashMap<>();
                    presenceData.put("type", "PRESENCE_CHANGE");
                    presenceData.put("userId", friendIdStr);
                    presenceData.put("status", status);

                    messagingTemplate.convertAndSendToUser(
                        userId,
                        "/queue/presence",
                        presenceData
                    );
                    logger.info("[Presence] 推送好友 {} 状态({}) 给用户 {}", friendIdStr, status, userId);
                }
            } catch (Exception e) {
                logger.error("[Presence] 推送好友状态失败", e);
            }
        }).start();
    }

    /**
     * 广播用户状态变化给所有好友
     */
    private void broadcastPresenceChange(String userId, String status) {
        try {
            // 查询该用户的所有好友
            List<BigInteger> friendIds = friendMapper.selectFriendIdsByUserId(new BigInteger(userId));

            logger.info("[Presence] 用户 {} 状态变为 {}, 广播给 {} 个好友", userId, status, friendIds.size());

            // 构建状态消息
            Map<String, Object> presenceData = new HashMap<>();
            presenceData.put("type", "PRESENCE_CHANGE");
            presenceData.put("userId", userId);
            presenceData.put("status", status);

            // 发送给每个在线的好友
            for (BigInteger friendId : friendIds) {
                String friendIdStr = friendId.toString();
                logger.info("[Presence] 检查好友是否在线: friendId={}, online={}", friendIdStr, userPresenceManager.isOnline(friendIdStr));
                if (userPresenceManager.isOnline(friendIdStr)) {
                    // 好友在线，发送到好友的队列
                    messagingTemplate.convertAndSendToUser(
                        friendIdStr,
                        "/queue/presence",
                        presenceData
                    );
                    logger.info("[Presence] 状态变化已发送给好友: {}", friendIdStr);
                }
            }
        } catch (Exception e) {
            logger.error("[Presence] 广播状态变化失败", e);
        }
    }
}

