# 千语聊天室 - 后端接口文档

本文档详细说明前端需要对接的所有后端接口，供后端开发参考。

## 基础配置

- **API基础地址**: `http://localhost:8080/api`
- **WebSocket地址**: `ws://localhost:8080/ws`
- **认证方式**: Bearer Token（JWT），在请求头中携带：`Authorization: Bearer {token}`

## 统一响应格式

### 成功响应
```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 错误响应
```json
{
  "code": 400,
  "message": "错误信息"
}
```

### 常见状态码
- `200`: 成功
- `400`: 请求参数错误
- `401`: 未授权（Token无效或过期）
- `404`: 资源不存在
- `500`: 服务器内部错误

## 一、用户相关接口

### 1.1 用户注册

**接口**: `POST /api/user/register`

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "string",  // 必填，用户名，3-20个字符
  "nickname": "string",  // 必填，昵称
  "password": "string"   // 必填，密码，至少6个字符
}
```

**响应**:
```json
{
  "code": 200,
  "message": "注册成功",
  "data": null
}
```

### 1.2 用户登录

**接口**: `POST /api/user/login`

**请求头**: 无需认证

**请求体**:
```json
{
  "username": "string",  // 必填
  "password": "string"   // 必填
}
```

**响应**:
```json
{
  "code": 200,
  "message": "登录成功",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userInfo": {
      "id": 1,
      "username": "testuser",
      "nickname": "测试用户",
      "avatar": "http://example.com/avatar.jpg"
    }
  }
}
```

### 1.3 用户登出

**接口**: `POST /api/user/logout`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "登出成功",
  "data": null
}
```

### 1.4 获取当前用户信息

**接口**: `GET /api/user/current`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "测试用户",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

### 1.5 更新用户信息

**接口**: `PUT /api/user/update`

**请求头**: 需要认证

**请求体**:
```json
{
  "nickname": "string",
  "avatar": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "username": "testuser",
    "nickname": "新昵称",
    "avatar": "http://example.com/avatar.jpg"
  }
}
```

### 1.6 搜索用户

**接口**: `GET /api/user/search`

**请求头**: 需要认证

**查询参数**:
- `keyword`: string，搜索关键词

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "username": "user2",
      "nickname": "用户2",
      "avatar": "http://example.com/avatar2.jpg"
    }
  ]
}
```

## 二、好友相关接口

### 2.1 获取好友列表

**接口**: `GET /api/friend/list`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 2,
      "nickname": "好友1",
      "avatar": "http://example.com/avatar1.jpg",
      "status": "在线",
      "groupId": 1  // 所属分组ID，可选
    }
  ]
}
```

### 2.2 获取好友分组列表

**接口**: `GET /api/friend/groups`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "我的好友",
      "friends": [
        {
          "id": 2,
          "nickname": "好友1",
          "avatar": "http://example.com/avatar1.jpg"
        }
      ]
    }
  ]
}
```

### 2.3 添加好友

**接口**: `POST /api/friend/add`

**请求头**: 需要认证

**请求体**:
```json
{
  "username": "string",  // 要添加的用户名
  "message": "string"    // 验证消息，可选
}
```

**响应**:
```json
{
  "code": 200,
  "message": "好友申请已发送",
  "data": {
    "id": 3,
    "nickname": "新好友",
    "avatar": "http://example.com/avatar3.jpg"
  }
}
```

### 2.4 删除好友

**接口**: `DELETE /api/friend/delete/{friendId}`

**请求头**: 需要认证

**路径参数**:
- `friendId`: number，好友ID

**响应**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 2.5 创建好友分组

**接口**: `POST /api/friend/group/create`

**请求头**: 需要认证

**请求体**:
```json
{
  "name": "string"  // 分组名称
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "name": "同事",
    "friends": []
  }
}
```

### 2.6 更新好友分组

**接口**: `PUT /api/friend/group/update/{groupId}`

**请求头**: 需要认证

**路径参数**:
- `groupId`: number，分组ID

**请求体**:
```json
{
  "name": "string"  // 新名称
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 2,
    "name": "新名称",
    "friends": []
  }
}
```

### 2.7 删除好友分组

**接口**: `DELETE /api/friend/group/delete/{groupId}`

**请求头**: 需要认证

**路径参数**:
- `groupId`: number，分组ID

**响应**:
```json
{
  "code": 200,
  "message": "删除成功",
  "data": null
}
```

### 2.8 移动好友到分组

**接口**: `PUT /api/friend/group/move`

**请求头**: 需要认证

**请求体**:
```json
{
  "friendId": 2,
  "groupId": 1  // 目标分组ID，null表示移除分组
}
```

**响应**:
```json
{
  "code": 200,
  "message": "移动成功",
  "data": null
}
```

### 2.9 获取好友申请列表

**接口**: `GET /api/friend/requests`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "applicantId": 3,
      "applicantName": "申请人",
      "message": "验证消息",
      "status": "pending",  // pending, accepted, rejected
      "createTime": 1234567890
    }
  ]
}
```

### 2.10 处理好友申请

**接口**: `PUT /api/friend/request/{requestId}`

**请求头**: 需要认证

**路径参数**:
- `requestId`: number，申请ID

**请求体**:
```json
{
  "action": "accept"  // accept 或 reject
}
```

**响应**:
```json
{
  "code": 200,
  "message": "处理成功",
  "data": null
}
```

## 三、消息相关接口

### 3.1 获取私聊消息历史

**接口**: `GET /api/message/private`

**请求头**: 需要认证

**查询参数**:
- `friendId`: number，好友ID
- `page`: number，页码，默认1
- `pageSize`: number，每页数量，默认50

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "senderId": 1,
      "receiverId": 2,
      "content": "消息内容",
      "timestamp": 1234567890,
      "status": "sent"  // sent, delivered, read
    }
  ]
}
```

### 3.2 获取群聊消息历史

**接口**: `GET /api/message/group`

**请求头**: 需要认证

**查询参数**:
- `groupId`: number，群组ID
- `page`: number，页码，默认1
- `pageSize`: number，每页数量，默认50

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "groupId": 1,
      "senderId": 1,
      "senderName": "发送者",
      "content": "消息内容",
      "timestamp": 1234567890
    }
  ]
}
```

### 3.3 标记消息为已读

**接口**: `PUT /api/message/read/{messageId}`

**请求头**: 需要认证

**路径参数**:
- `messageId`: number，消息ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

### 3.4 获取未读消息数

**接口**: `GET /api/message/unread/count`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "friendId": 5,      // 好友ID对应的未读数
    "groupId": 3       // 群组ID对应的未读数
  }
}
```

## 四、群组相关接口

### 4.1 获取群组列表

**接口**: `GET /api/group/list`

**请求头**: 需要认证

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "name": "技术交流群",
      "description": "技术讨论",
      "avatar": "http://example.com/group1.jpg",
      "memberCount": 10,
      "ownerId": 1
    }
  ]
}
```

### 4.2 创建群组

**接口**: `POST /api/group/create`

**请求头**: 需要认证

**请求体**:
```json
{
  "name": "string",      // 必填，群名称
  "description": "string" // 可选，群描述
}
```

**响应**:
```json
{
  "code": 200,
  "message": "创建成功",
  "data": {
    "id": 2,
    "name": "新群组",
    "description": "群描述",
    "avatar": "http://example.com/group2.jpg",
    "memberCount": 1,
    "ownerId": 1
  }
}
```

### 4.3 加入群组

**接口**: `POST /api/group/join/{groupId}`

**请求头**: 需要认证

**路径参数**:
- `groupId`: number，群组ID

**响应**:
```json
{
  "code": 200,
  "message": "加入成功",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "技术讨论",
    "avatar": "http://example.com/group1.jpg",
    "memberCount": 11,
    "ownerId": 1
  }
}
```

### 4.4 退出群组

**接口**: `DELETE /api/group/exit/{groupId}`

**请求头**: 需要认证

**路径参数**:
- `groupId`: number，群组ID

**响应**:
```json
{
  "code": 200,
  "message": "退出成功",
  "data": null
}
```

### 4.5 获取群组信息

**接口**: `GET /api/group/info/{groupId}`

**请求头**: 需要认证

**路径参数**:
- `groupId`: number，群组ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "id": 1,
    "name": "技术交流群",
    "description": "技术讨论",
    "avatar": "http://example.com/group1.jpg",
    "memberCount": 10,
    "ownerId": 1,
    "createTime": 1234567890
  }
}
```

### 4.6 获取群成员列表

**接口**: `GET /api/group/members/{groupId}`

**请求头**: 需要认证

**路径参数**:
- `groupId`: number，群组ID

**响应**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "id": 1,
      "nickname": "成员1",
      "avatar": "http://example.com/avatar1.jpg",
      "role": "owner"  // owner, admin, member
    }
  ]
}
```

### 4.7 更新群组信息

**接口**: `PUT /api/group/update/{groupId}`

**请求头**: 需要认证（仅群主或管理员）

**路径参数**:
- `groupId`: number，群组ID

**请求体**:
```json
{
  "name": "string",
  "description": "string",
  "avatar": "string"
}
```

**响应**:
```json
{
  "code": 200,
  "message": "更新成功",
  "data": {
    "id": 1,
    "name": "新名称",
    "description": "新描述",
    "avatar": "http://example.com/new-avatar.jpg",
    "memberCount": 10,
    "ownerId": 1
  }
}
```

## 五、WebSocket消息格式

### 5.1 连接建立

WebSocket连接URL格式：
```
ws://localhost:8080/ws?token={JWT_TOKEN}
```

连接成功后，服务器可以发送连接确认消息：
```json
{
  "type": "CONNECTED",
  "data": {
    "message": "连接成功"
  }
}
```

### 5.2 客户端发送消息

#### 私聊消息
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "receiverId": 2,
    "content": "你好",
    "timestamp": 1234567890
  }
}
```

#### 群聊消息
```json
{
  "type": "GROUP_MESSAGE",
  "data": {
    "groupId": 1,
    "content": "大家好",
    "timestamp": 1234567890
  }
}
```

### 5.3 服务器推送消息

#### 私聊消息
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "id": 1,
    "senderId": 2,
    "receiverId": 1,
    "senderName": "发送者昵称",
    "content": "消息内容",
    "timestamp": 1234567890
  }
}
```

#### 群聊消息
```json
{
  "type": "GROUP_MESSAGE",
  "data": {
    "id": 1,
    "groupId": 1,
    "senderId": 2,
    "senderName": "发送者昵称",
    "content": "消息内容",
    "timestamp": 1234567890
  }
}
```

#### 好友申请通知
```json
{
  "type": "FRIEND_REQUEST",
  "data": {
    "id": 1,
    "applicantId": 3,
    "applicantName": "申请人",
    "message": "验证消息",
    "timestamp": 1234567890
  }
}
```

#### 好友申请处理结果
```json
{
  "type": "FRIEND_REQUEST_RESULT",
  "data": {
    "requestId": 1,
    "action": "accepted",  // accepted 或 rejected
    "friendInfo": {
      "id": 3,
      "nickname": "新好友",
      "avatar": "http://example.com/avatar3.jpg"
    }
  }
}
```

#### 用户上线/下线通知
```json
{
  "type": "USER_STATUS",
  "data": {
    "userId": 2,
    "status": "online"  // online 或 offline
  }
}
```

### 5.4 心跳机制（可选）

客户端可以定期发送心跳包保持连接：
```json
{
  "type": "PING",
  "data": {
    "timestamp": 1234567890
  }
}
```

服务器响应：
```json
{
  "type": "PONG",
  "data": {
    "timestamp": 1234567890
  }
}
```

## 六、注意事项

1. **Token认证**: 除了登录和注册接口，所有接口都需要在请求头中携带Token
2. **时间戳**: 所有时间戳使用Unix时间戳（毫秒）
3. **分页**: 消息列表接口支持分页，默认每页50条
4. **实时性**: 私聊和群聊消息通过WebSocket实时推送，历史消息通过HTTP接口获取
5. **错误处理**: 所有接口错误都返回统一的错误格式，前端会根据code进行相应处理
6. **WebSocket重连**: 前端实现了自动重连机制，最多重试5次
7. **消息状态**: 私聊消息可以标记为已读，群聊消息可以标记为已送达

## 七、开发建议

1. 建议使用JWT作为Token认证机制
2. WebSocket连接建议使用STOMP协议或自定义协议
3. 消息存储建议使用Redis作为缓存，MySQL作为持久化存储
4. 建议实现消息队列处理高并发场景
5. 建议实现消息去重机制，避免重复消息
6. 建议实现消息分页加载，避免一次性加载过多消息

