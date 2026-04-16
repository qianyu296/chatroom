package com.qianyu.chatroom.controller;

import com.qianyu.chatroom.entry.GroupMessage;
import com.qianyu.chatroom.entry.PrivateMessage;
import com.qianyu.chatroom.service.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.math.BigInteger;
import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket消息处理控制器
 * 处理STOMP协议的消息
 */
@Controller
public class WebSocketController {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageService messageService;

    /**
     * 处理私聊消息
     * 客户端发送到: /app/chat/private
     * 服务端转发到: /user/{receiverId}/queue/private
     */
    @MessageMapping("/chat/private")
    public Map<String, Object> handlePrivateMessage(@Payload Map<String, Object> messageData,
                                                    Principal principal) {
        try {
            // 从Principal获取当前登录用户ID
            if (principal == null) {
                logger.warn("未找到发送者ID，消息发送失败");
                return createErrorResponse("未授权");
            }
            BigInteger senderId = new BigInteger(principal.getName());

            // 从消息中获取接收者ID和内容
            Object receiverIdObj = messageData.get("receiverId");
            Object contentObj = messageData.get("content");
            Object timestampObj = messageData.get("timestamp");

            if (receiverIdObj == null || contentObj == null) {
                logger.warn("消息格式错误，缺少必要字段");
                return createErrorResponse("消息格式错误");
            }

            BigInteger receiverId = new BigInteger(receiverIdObj.toString());
            String content = contentObj.toString();
            Long timestamp = timestampObj != null ? Long.parseLong(timestampObj.toString()) : System.currentTimeMillis();

            logger.info("收到私聊消息: 发送者={}, 接收者={}, 内容={}", senderId, receiverId, content);

            // 保存消息到数据库
            PrivateMessage savedMessage = messageService.savePrivateMessage(
                senderId, receiverId, content, "text", timestamp
            );

            // 构建消息数据
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("id", savedMessage.getId());
            messagePayload.put("senderId", senderId);
            messagePayload.put("receiverId", receiverId);
            messagePayload.put("content", content);
            messagePayload.put("timestamp", timestamp);

            // 构建响应消息（嵌套结构，前端期望 { type, data: {...} }）
            Map<String, Object> response = new HashMap<>();
            response.put("type", "PRIVATE_MESSAGE");
            response.put("data", messagePayload);

            // 发送给接收者
            logger.info("发送私聊消息给用户: {}", receiverId);
            messagingTemplate.convertAndSendToUser(
                receiverId.toString(),
                "/queue/private",
                response
            );

            // 同时发送给发送者（确认消息已发送）
            logger.info("发送私聊消息给发送者: {}", senderId);
            messagingTemplate.convertAndSendToUser(
                senderId.toString(),
                "/queue/private",
                response
            );

            return response;
        } catch (Exception e) {
            logger.error("处理私聊消息失败", e);
            return createErrorResponse("处理消息失败: " + e.getMessage());
        }
    }

    /**
     * 处理群聊消息
     * 客户端发送到: /app/chat/group
     * 服务端广播到: /topic/group/{groupId}
     */
    @MessageMapping("/chat/group")
    public Map<String, Object> handleGroupMessage(@Payload Map<String, Object> messageData,
                                                  Principal principal) {
        try {
            // 从Principal获取当前登录用户ID
            if (principal == null) {
                logger.warn("未找到发送者ID，消息发送失败");
                return createErrorResponse("未授权");
            }
            BigInteger senderId = new BigInteger(principal.getName());

            // 从消息中获取群组ID和内容
            Object groupIdObj = messageData.get("groupId");
            Object contentObj = messageData.get("content");
            Object timestampObj = messageData.get("timestamp");

            if (groupIdObj == null || contentObj == null) {
                logger.warn("消息格式错误，缺少必要字段");
                return createErrorResponse("消息格式错误");
            }

            BigInteger groupId = new BigInteger(groupIdObj.toString());
            String content = contentObj.toString();
            Long timestamp = timestampObj != null ? Long.parseLong(timestampObj.toString()) : System.currentTimeMillis();

            logger.info("收到群聊消息: 群组={}, 发送者={}, 内容={}", groupId, senderId, content);

            // 保存消息到数据库
            GroupMessage savedMessage = messageService.saveGroupMessage(
                groupId, senderId, content, "text", timestamp
            );

            // 构建消息数据
            Map<String, Object> messagePayload = new HashMap<>();
            messagePayload.put("id", savedMessage.getId());
            messagePayload.put("groupId", groupId);
            messagePayload.put("senderId", senderId);
            messagePayload.put("content", content);
            messagePayload.put("timestamp", timestamp);

            // 构建响应消息（嵌套结构，前端期望 { type, data: {...} }）
            Map<String, Object> response = new HashMap<>();
            response.put("type", "GROUP_MESSAGE");
            response.put("data", messagePayload);

            // 广播到群组主题
            messagingTemplate.convertAndSend("/topic/group/" + groupId, response);

            return response;
        } catch (Exception e) {
            logger.error("处理群聊消息失败", e);
            return createErrorResponse("处理消息失败: " + e.getMessage());
        }
    }

    /**
     * 创建错误响应
     */
    private Map<String, Object> createErrorResponse(String errorMessage) {
        Map<String, Object> response = new HashMap<>();
        response.put("type", "ERROR");
        response.put("message", errorMessage);
        return response;
    }
}
