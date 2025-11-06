# WebSocket 学习总结

## 一、WebSocket 概述

### 1. 什么是 WebSocket？
WebSocket 是一种在单个 TCP 连接上进行**全双工、双向通信**的协议，适用于**实时通信场景**。

### 2. WebSocket 与 HTTP 的区别
| 特性       | HTTP                  | WebSocket                     |
|------------|-----------------------|-------------------------------|
| 连接方式   | 短连接，每次请求需建立连接 | 长连接，一次握手，持续通信     |
| 通信方向   | 客户端发起请求         | 客户端和服务端可互相推送消息  |
| 实时性     | 较差（依赖轮询）       | 高（实时性高）               |
| 协议       | 请求-响应             | 基于帧的协议                  |

### 3. WebSocket 适用场景
- 实时聊天（IM、客服）
- 多人协作 / 在线文档
- 实时数据（股票、监控）
- 在线游戏
- IoT 设备通信

---

## 二、WebSocket 通信原理

1. **握手阶段**
   - 客户端通过 HTTP Upgrade 请求升级为 WebSocket
   - 服务端响应 101 状态码，完成协议升级
2. **通信阶段**
   - 连接建立后，双方可随时互发消息帧，无需重复握手

### 握手过程示例

**客户端请求**:
```http
GET /ws HTTP/1.1
Host: localhost:8080
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Key: dGhlIHNhbXBsZSBub25jZQ==
Sec-WebSocket-Version: 13
```

**服务端响应**:
```http
HTTP/1.1 101 Switching Protocols
Upgrade: websocket
Connection: Upgrade
Sec-WebSocket-Accept: s3pPLMBiTxaQ9kYGzzhZRbK+xOo=
```

---

## 三、Java + Spring Boot 中的 WebSocket（STOMP）

### 1. 核心组件
- `@EnableWebSocketMessageBroker`：启用 STOMP 消息代理
- `WebSocketMessageBrokerConfigurer`：配置 STOMP 端点与消息代理
- `@MessageMapping`：处理客户端发送的消息
- `@SendTo`：广播消息到某个主题
- `@SendToUser`：发送消息给当前用户
- `SimpMessagingTemplate`：手动向主题或用户发送消息

### 2. 消息代理地址
- `/topic/xxx`：广播（多个订阅者）
- `/queue/xxx`：队列（通常用于私人消息）
- `/user/xxx/queue/yyy`：定向私人消息

### 3. Spring Boot WebSocket 配置代码

#### 3.1 添加依赖（pom.xml）
```xml
<dependencies>
    <!-- Spring Boot WebSocket -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    
    <!-- Spring Boot Security（用于用户认证） -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
</dependencies>
```

#### 3.2 WebSocket 配置类
```java
package com.chatroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端发送消息
        // /topic 用于广播消息
        // /queue 用于点对点消息
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置客户端发送消息的前缀
        // 客户端发送消息到 /app/xxx 会被路由到对应的 @MessageMapping
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户消息前缀
        // 用于点对点消息，格式：/user/{username}/queue/xxx
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册 STOMP 端点
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册 WebSocket 端点，客户端通过此端点连接
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域（生产环境应指定具体域名）
                .withSockJS(); // 启用 SockJS 支持（可选，提供降级方案）
        
        // 也可以不使用 SockJS
        // registry.addEndpoint("/ws").setAllowedOriginPatterns("*");
    }
}
```

#### 3.3 WebSocket 安全配置（可选，用于用户认证）
```java
package com.chatroom.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

@Configuration
public class WebSocketSecurityConfig implements ChannelInterceptor {

    private final UserDetailsService userDetailsService;

    public WebSocketSecurityConfig(UserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从请求头获取 token
            String token = accessor.getFirstNativeHeader("Authorization");
            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                // 验证 token 并设置用户信息
                UserDetails userDetails = userDetailsService.loadUserByUsername(getUsernameFromToken(token));
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                accessor.setUser(auth);
            }
        }
        
        return message;
    }
    
    // 从 token 中提取用户名（需要根据你的 JWT 实现）
    private String getUsernameFromToken(String token) {
        // 解析 JWT token 获取用户名
        // 这里需要实现你的 JWT 解析逻辑
        return "username";
    }
}
```

---

## 四、学习路径

### Level 1：了解 WebSocket 基础
- 什么是 WebSocket
- 与 HTTP 的区别
- 适用场景

### Level 2：前端基础
- 原生 WebSocket API
- 使用 stomp.js 连接服务端
- 订阅与发送消息

#### 2.1 原生 WebSocket 示例（前端）

```javascript
// 创建 WebSocket 连接
const ws = new WebSocket('ws://localhost:8080/ws');

// 连接打开时
ws.onopen = function(event) {
    console.log('WebSocket 连接已建立');
    // 发送消息
    ws.send('Hello Server!');
};

// 接收消息
ws.onmessage = function(event) {
    console.log('收到消息:', event.data);
    const message = JSON.parse(event.data);
    console.log('消息内容:', message);
};

// 连接关闭时
ws.onclose = function(event) {
    console.log('WebSocket 连接已关闭');
};

// 连接错误时
ws.onerror = function(error) {
    console.error('WebSocket 错误:', error);
};

// 发送消息
function sendMessage(message) {
    if (ws.readyState === WebSocket.OPEN) {
        ws.send(JSON.stringify(message));
    } else {
        console.error('WebSocket 未连接');
    }
}
```

#### 2.2 使用 STOMP.js 示例（前端）

```html
<!DOCTYPE html>
<html>
<head>
    <title>WebSocket 测试</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <div id="messages"></div>
    <input type="text" id="messageInput" />
    <button onclick="sendMessage()">发送</button>

    <script>
        // 连接配置
        const socket = new SockJS('http://localhost:8080/ws');
        const stompClient = Stomp.over(socket);

        // 连接选项
        const headers = {
            Authorization: 'Bearer your-jwt-token' // 如果需要认证
        };

        // 连接到服务器
        stompClient.connect(headers, function(frame) {
            console.log('连接成功: ' + frame);
            
            // 订阅广播主题
            stompClient.subscribe('/topic/public', function(message) {
                const body = JSON.parse(message.body);
                console.log('收到广播消息:', body);
                displayMessage(body);
            });
            
            // 订阅私人消息
            stompClient.subscribe('/user/queue/private', function(message) {
                const body = JSON.parse(message.body);
                console.log('收到私人消息:', body);
                displayMessage(body);
            });
        }, function(error) {
            console.error('连接失败:', error);
        });

        // 发送消息到服务器
        function sendMessage() {
            const input = document.getElementById('messageInput');
            const message = {
                type: 'CHAT',
                content: input.value,
                sender: 'user1',
                timestamp: Date.now()
            };
            
            // 发送到 /app/chat（对应后端的 @MessageMapping("/chat")）
            stompClient.send('/app/chat', {}, JSON.stringify(message));
            input.value = '';
        }

        // 显示消息
        function displayMessage(message) {
            const messagesDiv = document.getElementById('messages');
            const messageDiv = document.createElement('div');
            messageDiv.textContent = `${message.sender}: ${message.content}`;
            messagesDiv.appendChild(messageDiv);
        }

        // 断开连接
        function disconnect() {
            if (stompClient !== null) {
                stompClient.disconnect();
            }
        }
    </script>
</body>
</html>
```

### Level 3：后端基础（Spring Boot）
- 配置 WebSocket + STOMP
- 编写 Controller 处理消息
- 使用 @MessageMapping、@SendTo、SimpMessagingTemplate

#### 3.1 消息实体类

```java
package com.chatroom.dto;

import lombok.Data;

@Data
public class ChatMessage {
    private String type;        // 消息类型：PRIVATE_MESSAGE, GROUP_MESSAGE
    private Long senderId;       // 发送者ID
    private Long receiverId;     // 接收者ID（私聊）
    private Long groupId;       // 群组ID（群聊）
    private String content;      // 消息内容
    private Long timestamp;      // 时间戳
}
```

#### 3.2 WebSocket Controller

```java
package com.chatroom.controller;

import com.chatroom.dto.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;

    public ChatController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * 处理客户端发送的聊天消息
     * 客户端发送到 /app/chat
     */
    @MessageMapping("/chat")
    @SendTo("/topic/public")  // 广播到所有订阅者
    public ChatMessage handlePublicMessage(@Payload ChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        return message;
    }

    /**
     * 处理私聊消息
     */
    @MessageMapping("/private")
    public void handlePrivateMessage(@Payload ChatMessage message, Principal principal) {
        message.setTimestamp(System.currentTimeMillis());
        message.setSenderId(getUserIdFromPrincipal(principal));
        
        // 发送给指定用户
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/private",
            message
        );
    }

    /**
     * 处理群聊消息
     */
    @MessageMapping("/group")
    public void handleGroupMessage(@Payload ChatMessage message, Principal principal) {
        message.setTimestamp(System.currentTimeMillis());
        message.setSenderId(getUserIdFromPrincipal(principal));
        
        // 广播到群组
        messagingTemplate.convertAndSend("/topic/group/" + message.getGroupId(), message);
    }

    /**
     * 获取当前用户ID
     */
    private Long getUserIdFromPrincipal(Principal principal) {
        // 从 principal 中获取用户ID
        // 需要根据你的认证实现来获取
        return 1L;
    }
}
```

#### 3.3 事件监听器（连接/断开）

```java
package com.chatroom.listener;

import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketEventListener {

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        System.out.println("收到新的 WebSocket 连接");
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String username = (String) headerAccessor.getSessionAttributes().get("username");
        
        if (username != null) {
            System.out.println("用户断开连接: " + username);
            
            // 可以在这里通知其他用户该用户下线
            // messagingTemplate.convertAndSend("/topic/public", disconnectMessage);
        }
    }
}
```

### Level 4：进阶实战
- 用户身份识别
- 聊天室 / 群组功能
- 消息存储
- 安全与性能优化

#### 4.1 用户身份识别完整实现

```java
package com.chatroom.config;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

public class AuthChannelInterceptor implements ChannelInterceptor {

    private final UserDetailsService userDetailsService;
    private final JwtTokenUtil jwtTokenUtil;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        
        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            // 从 URL 参数获取 token
            String token = accessor.getFirstNativeHeader("token");
            
            if (token != null && jwtTokenUtil.validateToken(token)) {
                String username = jwtTokenUtil.getUsernameFromToken(token);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
                accessor.setUser(auth);
            }
        }
        
        return message;
    }
}
```

#### 4.2 聊天室功能实现

```java
package com.chatroom.service;

import com.chatroom.dto.ChatMessage;
import com.chatroom.entity.GroupMessage;
import com.chatroom.repository.GroupMessageRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class ChatService {

    private final SimpMessagingTemplate messagingTemplate;
    private final GroupMessageRepository messageRepository;

    public ChatService(SimpMessagingTemplate messagingTemplate,
                      GroupMessageRepository messageRepository) {
        this.messagingTemplate = messagingTemplate;
        this.messageRepository = messageRepository;
    }

    /**
     * 发送群聊消息
     */
    public void sendGroupMessage(Long groupId, ChatMessage message) {
        // 保存到数据库
        GroupMessage groupMessage = new GroupMessage();
        groupMessage.setGroupId(groupId);
        groupMessage.setSenderId(message.getSenderId());
        groupMessage.setContent(message.getContent());
        groupMessage.setTimestamp(message.getTimestamp());
        messageRepository.save(groupMessage);
        
        // 推送给群组所有成员
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }

    /**
     * 发送私聊消息
     */
    public void sendPrivateMessage(Long receiverId, ChatMessage message) {
        // 保存到数据库
        // ...
        
        // 推送给指定用户
        messagingTemplate.convertAndSendToUser(
            receiverId.toString(),
            "/queue/private",
            message
        );
    }
}
```

#### 4.3 消息存储实现

```java
package com.chatroom.repository;

import com.chatroom.entity.GroupMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GroupMessageRepository extends JpaRepository<GroupMessage, Long> {
    
    @Query("SELECT m FROM GroupMessage m WHERE m.groupId = :groupId ORDER BY m.timestamp DESC")
    Page<GroupMessage> findByGroupIdOrderByTimestampDesc(
        @Param("groupId") Long groupId, 
        Pageable pageable
    );
}
```

---

## 五、练习题

### 初级
- 使用原生 WebSocket 连接服务，实现简单收发
- 理解 WebSocket 与 HTTP 的区别

**练习1：简单聊天服务端**

```java
@Controller
public class SimpleChatController {
    
    @MessageMapping("/simple")
    @SendTo("/topic/messages")
    public String handleMessage(String message) {
        return "服务器收到: " + message;
    }
}
```

**练习2：定时推送服务端**

```java
@Component
public class ScheduledTask {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Scheduled(fixedRate = 1000) // 每秒推送一次
    public void sendTime() {
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        messagingTemplate.convertAndSend("/topic/time", time);
    }
}
```

### 中级
- 实现服务端定时推送时间
- 实现群聊（广播）
- 实现私人消息（定向推送）

**练习3：群聊实现**

```java
@Controller
public class GroupChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @MessageMapping("/group/{groupId}")
    public void sendGroupMessage(@DestinationVariable Long groupId, 
                                @Payload ChatMessage message) {
        message.setTimestamp(System.currentTimeMillis());
        // 广播到群组
        messagingTemplate.convertAndSend("/topic/group/" + groupId, message);
    }
}
```

**练习4：私聊实现**

```java
@MessageMapping("/private")
public void sendPrivateMessage(@Payload ChatMessage message, Principal principal) {
    message.setTimestamp(System.currentTimeMillis());
    // 发送给指定用户
    messagingTemplate.convertAndSendToUser(
        message.getReceiverId().toString(),
        "/queue/private",
        message
    );
}
```

### 高级
- 实现多用户聊天室（群聊 + 私聊）
- 结合数据库存储消息
- 实现用户鉴权与权限控制

**练习5：完整的聊天室实现**

```java
@Service
@Transactional
public class ChatRoomService {
    
    private final PrivateMessageRepository privateMessageRepository;
    private final GroupMessageRepository groupMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    
    /**
     * 发送私聊消息并存储
     */
    public void sendAndSavePrivateMessage(ChatMessage message) {
        // 保存到数据库
        PrivateMessage privateMessage = new PrivateMessage();
        privateMessage.setSenderId(message.getSenderId());
        privateMessage.setReceiverId(message.getReceiverId());
        privateMessage.setContent(message.getContent());
        privateMessage.setTimestamp(message.getTimestamp());
        privateMessage.setStatus("sent");
        privateMessageRepository.save(privateMessage);
        
        // 推送给接收者
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/private",
            message
        );
    }
    
    /**
     * 获取私聊消息历史
     */
    public Page<PrivateMessage> getPrivateMessageHistory(
            Long userId, Long friendId, Pageable pageable) {
        return privateMessageRepository.findBySenderIdAndReceiverIdOrReceiverIdAndSenderId(
            userId, friendId, userId, friendId, pageable
        );
    }
}
```

**练习6：用户鉴权实现**

```java
@Configuration
public class WebSocketSecurityConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {
                StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
                
                if (StompCommand.CONNECT.equals(accessor.getCommand())) {
                    // 验证 token
                    String token = accessor.getFirstNativeHeader("token");
                    if (token != null && validateToken(token)) {
                        // 设置用户信息
                        Authentication auth = getAuthenticationFromToken(token);
                        accessor.setUser(auth);
                    } else {
                        throw new MessagingException("未授权");
                    }
                }
                
                return message;
            }
        });
    }
}
```

---

## 六、完整项目示例

### 6.1 前端完整实现（Vue.js）

```javascript
// utils/websocket.js
import SockJS from 'sockjs-client'
import { Stomp } from 'stompjs'

class WebSocketManager {
  constructor() {
    this.stompClient = null
    this.connected = false
  }

  connect(token) {
    const socket = new SockJS('http://localhost:8080/ws')
    this.stompClient = Stomp.over(socket)
    
    // 禁用调试日志
    this.stompClient.debug = null
    
    const headers = {
      Authorization: `Bearer ${token}`
    }
    
    this.stompClient.connect(headers, (frame) => {
      console.log('WebSocket连接成功:', frame)
      this.connected = true
      this.onConnected()
    }, (error) => {
      console.error('WebSocket连接失败:', error)
      this.connected = false
    })
  }

  subscribe(topic, callback) {
    if (this.stompClient && this.connected) {
      return this.stompClient.subscribe(topic, (message) => {
        const data = JSON.parse(message.body)
        callback(data)
      })
    }
  }

  send(destination, message) {
    if (this.stompClient && this.connected) {
      this.stompClient.send(destination, {}, JSON.stringify(message))
    }
  }

  disconnect() {
    if (this.stompClient) {
      this.stompClient.disconnect()
      this.connected = false
    }
  }

  onConnected() {
    // 连接成功后的回调，可以在这里订阅主题
  }
}

export default new WebSocketManager()
```

### 6.2 后端完整实现（Spring Boot）

```java
// config/WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic", "/queue");
        config.setApplicationDestinationPrefixes("/app");
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }
}

// controller/ChatController.java
@Controller
public class ChatController {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    
    @MessageMapping("/private")
    public void handlePrivateMessage(@Payload ChatMessage message, Principal principal) {
        Long senderId = getUserId(principal);
        message.setSenderId(senderId);
        chatService.sendPrivateMessage(message);
    }
    
    @MessageMapping("/group")
    public void handleGroupMessage(@Payload ChatMessage message, Principal principal) {
        Long senderId = getUserId(principal);
        message.setSenderId(senderId);
        chatService.sendGroupMessage(message);
    }
    
    private Long getUserId(Principal principal) {
        // 从 principal 中获取用户ID
        return Long.parseLong(principal.getName());
    }
}

// service/ChatService.java
@Service
public class ChatService {
    
    private final SimpMessagingTemplate messagingTemplate;
    private final PrivateMessageRepository privateMessageRepository;
    
    public void sendPrivateMessage(ChatMessage message) {
        // 保存消息
        PrivateMessage msg = new PrivateMessage();
        msg.setSenderId(message.getSenderId());
        msg.setReceiverId(message.getReceiverId());
        msg.setContent(message.getContent());
        msg.setTimestamp(System.currentTimeMillis());
        privateMessageRepository.save(msg);
        
        // 推送消息
        messagingTemplate.convertAndSendToUser(
            message.getReceiverId().toString(),
            "/queue/private",
            message
        );
    }
    
    public void sendGroupMessage(ChatMessage message) {
        // 保存消息
        GroupMessage msg = new GroupMessage();
        msg.setGroupId(message.getGroupId());
        msg.setSenderId(message.getSenderId());
        msg.setContent(message.getContent());
        msg.setTimestamp(System.currentTimeMillis());
        groupMessageRepository.save(msg);
        
        // 推送到群组
        messagingTemplate.convertAndSend("/topic/group/" + message.getGroupId(), message);
    }
}
```

### 6.3 消息格式定义

```java
// dto/ChatMessage.java
@Data
public class ChatMessage {
    private String type;           // PRIVATE_MESSAGE, GROUP_MESSAGE
    private Long senderId;
    private Long receiverId;       // 私聊时使用
    private Long groupId;          // 群聊时使用
    private String senderName;
    private String content;
    private Long timestamp;
}
```

### 6.4 前端使用示例

```vue
<template>
  <div>
    <div v-for="message in messages" :key="message.id">
      {{ message.senderName }}: {{ message.content }}
    </div>
    <input v-model="inputMessage" @keyup.enter="send" />
  </div>
</template>

<script>
import wsManager from '@/utils/websocket'

export default {
  data() {
    return {
      messages: [],
      inputMessage: '',
      currentChatId: null
    }
  },
  mounted() {
    // 连接WebSocket
    const token = this.$store.state.user.token
    wsManager.connect(token)
    
    // 订阅私聊消息
    wsManager.subscribe('/user/queue/private', (message) => {
      this.messages.push(message)
    })
    
    // 订阅群聊消息
    wsManager.subscribe('/topic/group/' + this.currentChatId, (message) => {
      this.messages.push(message)
    })
  },
  methods: {
    send() {
      const message = {
        type: 'PRIVATE_MESSAGE',
        receiverId: this.currentChatId,
        content: this.inputMessage,
        timestamp: Date.now()
      }
      wsManager.send('/app/private', message)
      this.inputMessage = ''
    }
  },
  beforeDestroy() {
    wsManager.disconnect()
  }
}
</script>
```

## 七、常见问题

### Q1: 如何处理 WebSocket 连接断开？
```javascript
// 前端自动重连
stompClient.onclose = function() {
    console.log('连接断开，5秒后重连...')
    setTimeout(() => {
        connect()
    }, 5000)
}
```

### Q2: 如何限制消息发送频率？
```java
// 使用令牌桶算法限流
@RateLimiter(name = "chat")
@MessageMapping("/chat")
public void handleMessage(@Payload ChatMessage message) {
    // ...
}
```

### Q3: 如何处理大量在线用户？
- 使用 Redis 作为消息代理
- 使用消息队列（RabbitMQ、Kafka）
- 实现消息分片和负载均衡

### Q4: 如何保证消息顺序？
- 使用消息队列保证顺序
- 在消息中添加序列号
- 客户端按序列号排序

## 八、学习资源

- [Spring WebSocket 官方文档](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#websocket)
- [MDN - WebSocket](https://developer.mozilla.org/zh-CN/docs/Web/API/WebSocket)
- [stomp.js 官方文档](https://stomp-js.github.io/stomp-websocket/)
- [Spring Boot WebSocket 实战教程](https://www.baeldung.com/websockets-spring)
- 搜索关键词：Spring Boot WebSocket 教程、STOMP 聊天室、实时通信

---