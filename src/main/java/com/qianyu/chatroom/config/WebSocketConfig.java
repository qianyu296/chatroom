package com.qianyu.chatroom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 配置STOMP消息代理和WebSocket端点
 */
@Configuration
@EnableWebSocketMessageBroker  // 启用WebSocket消息代理
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${cors.allowed-origins:http://localhost:8080,http://localhost:3000}")
    private String allowedOrigins;

    /**
     * 注册STOMP端点
     * 客户端通过此端点建立WebSocket连接
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，支持原生WebSocket和SockJS
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns(allowedOrigins.split(","))
                .withSockJS();  // 启用SockJS支持，提供降级方案

        // 也可以注册一个不使用SockJS的端点（可选）
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns(allowedOrigins.split(","));
    }

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 启用简单消息代理，用于向客户端发送消息
        // /topic 用于广播消息（多个订阅者）
        // /queue 用于点对点消息
        registry.enableSimpleBroker("/topic", "/queue");

        // 设置客户端发送消息的前缀
        // 客户端发送消息到 /app/xxx 会被路由到对应的 @MessageMapping
        registry.setApplicationDestinationPrefixes("/app");

        // 设置用户消息前缀
        // 用于点对点消息，格式：/user/{username}/queue/xxx
        registry.setUserDestinationPrefix("/user");
    }
}
