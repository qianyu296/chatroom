package com.qianyu.chatroom.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户在线状态管理器
 * 管理所有在线用户的会话
 */
@Component
public class UserPresenceManager {

    private static final Logger logger = LoggerFactory.getLogger(UserPresenceManager.class);

    // 在线用户存储: userId -> 会话数量
    private final ConcurrentHashMap<String, Integer> onlineUsers = new ConcurrentHashMap<>();

    /**
     * 用户上线
     */
    public void userOnline(String userId) {
        onlineUsers.merge(userId, 1, Integer::sum);
        logger.info("[Presence] 用户上线: {}, 当前会话数: {}", userId, onlineUsers.get(userId));
    }

    /**
     * 用户下线
     */
    public void userOffline(String userId) {
        logger.info("[Presence] userOffline 被调用, userId: {}", userId);
        onlineUsers.computeIfPresent(userId, (id, count) -> {
            int newCount = count - 1;
            if (newCount <= 0) {
                logger.info("[Presence] 用户完全下线: {}", id);
                return null; // 移除
            }
            logger.info("[Presence] 用户会话减少: {}, 剩余: {}", id, newCount);
            return newCount;
        });
    }

    /**
     * 检查用户是否在线
     */
    public boolean isOnline(String userId) {
        return onlineUsers.containsKey(userId);
    }

    /**
     * 获取用户在线状态
     */
    public String getStatus(String userId) {
        return isOnline(userId) ? "在线" : "离线";
    }

    /**
     * 获取所有在线用户ID
     */
    public Set<String> getAllOnlineUsers() {
        return onlineUsers.keySet();
    }
}
