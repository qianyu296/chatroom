# 千语聊天室

一个基于Vue 2的前后端分离聊天室应用，支持用户私聊和群聊功能。

## 功能特性

### 核心功能
- ✅ 用户私聊
- ✅ 用户群聊

### 基础功能
- ✅ 用户注册
- ✅ 用户登录
- ✅ 发送消息
- ✅ 添加好友
- ✅ 好友分组（接口已预留）

## 技术栈

- **前端框架**: Vue 2.6
- **路由管理**: Vue Router 3.6
- **状态管理**: Vuex 3.6
- **UI组件库**: Element UI 2.15
- **HTTP客户端**: Axios 1.6
- **实时通信**: WebSocket

## 项目结构

```
src/
├── api/              # API接口定义
│   ├── user.js      # 用户相关接口
│   ├── friend.js    # 好友相关接口
│   ├── message.js   # 消息相关接口
│   └── group.js     # 群组相关接口
├── components/      # 组件
│   ├── FriendList.vue
│   ├── GroupList.vue
│   ├── ChatWindow.vue
│   ├── AddFriendDialog.vue
│   └── CreateGroupDialog.vue
├── router/          # 路由配置
│   └── index.js
├── store/           # Vuex状态管理
│   ├── index.js
│   └── modules/
│       ├── user.js
│       ├── friend.js
│       ├── message.js
│       └── group.js
├── utils/           # 工具函数
│   ├── request.js   # Axios封装
│   └── websocket.js # WebSocket管理
├── views/           # 页面视图
│   ├── Login.vue
│   ├── Register.vue
│   └── Chat.vue
├── App.vue
└── main.js
```

## 安装依赖

```bash
npm install
```

## 开发环境配置

项目使用环境变量配置API地址，创建 `.env.development` 文件：

```
VUE_APP_API_BASE_URL=http://localhost:8080/api
VUE_APP_WS_URL=ws://localhost:8080/ws
```

## 运行项目

```bash
npm run serve
```

项目将在 `http://localhost:8080` 启动（端口可能不同，请查看终端输出）。

## 后端接口规范

### 用户相关接口

#### 1. 用户注册
- **接口**: `POST /api/user/register`
- **请求体**:
```json
{
  "username": "string",
  "nickname": "string",
  "password": "string"
}
```

#### 2. 用户登录
- **接口**: `POST /api/user/login`
- **请求体**:
```json
{
  "username": "string",
  "password": "string"
}
```
- **响应**:
```json
{
  "code": 200,
  "data": {
    "token": "string",
    "userInfo": {
      "id": "number",
      "username": "string",
      "nickname": "string",
      "avatar": "string"
    }
  }
}
```

#### 3. 获取当前用户信息
- **接口**: `GET /api/user/current`
- **请求头**: `Authorization: Bearer {token}`

### 好友相关接口

#### 1. 获取好友列表
- **接口**: `GET /api/friend/list`
- **响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "number",
      "nickname": "string",
      "avatar": "string",
      "status": "string"
    }
  ]
}
```

#### 2. 添加好友
- **接口**: `POST /api/friend/add`
- **请求体**:
```json
{
  "username": "string",
  "message": "string"
}
```

#### 3. 获取好友分组
- **接口**: `GET /api/friend/groups`
- **响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "number",
      "name": "string",
      "friends": []
    }
  ]
}
```

### 消息相关接口

#### 1. 获取私聊消息历史
- **接口**: `GET /api/message/private`
- **参数**: `friendId`, `page`, `pageSize`
- **响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "number",
      "senderId": "number",
      "receiverId": "number",
      "content": "string",
      "timestamp": "number"
    }
  ]
}
```

#### 2. 获取群聊消息历史
- **接口**: `GET /api/message/group`
- **参数**: `groupId`, `page`, `pageSize`

### 群组相关接口

#### 1. 获取群组列表
- **接口**: `GET /api/group/list`
- **响应**:
```json
{
  "code": 200,
  "data": [
    {
      "id": "number",
      "name": "string",
      "description": "string",
      "avatar": "string",
      "memberCount": "number"
    }
  ]
}
```

#### 2. 创建群组
- **接口**: `POST /api/group/create`
- **请求体**:
```json
{
  "name": "string",
  "description": "string"
}
```

## WebSocket消息格式

### 客户端发送消息

#### 私聊消息
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "receiverId": "number",
    "content": "string",
    "timestamp": "number"
  }
}
```

#### 群聊消息
```json
{
  "type": "GROUP_MESSAGE",
  "data": {
    "groupId": "number",
    "content": "string",
    "timestamp": "number"
  }
}
```

### 服务端推送消息

#### 私聊消息
```json
{
  "type": "PRIVATE_MESSAGE",
  "data": {
    "id": "number",
    "senderId": "number",
    "receiverId": "number",
    "content": "string",
    "timestamp": "number",
    "senderName": "string"
  }
}
```

#### 群聊消息
```json
{
  "type": "GROUP_MESSAGE",
  "data": {
    "id": "number",
    "groupId": "number",
    "senderId": "number",
    "content": "string",
    "timestamp": "number",
    "senderName": "string"
  }
}
```

## WebSocket连接

WebSocket连接URL格式：
```
ws://localhost:8080/ws?token={token}
```

连接建立后，客户端会自动发送心跳包保持连接（可选实现）。

## 注意事项

1. 所有API请求需要在请求头中携带Token（登录接口除外）：
   ```
   Authorization: Bearer {token}
   ```

2. Token存储在localStorage中，页面刷新后会自动恢复登录状态。

3. WebSocket连接在用户登录后自动建立，登出时自动关闭。

4. 消息发送采用乐观更新策略，发送后立即显示在界面上，等待服务器确认。

5. 未读消息数会在切换到对应聊天时自动清零。

## 开发说明

- 所有后端接口已预留，可根据实际后端实现调整接口地址和参数
- WebSocket消息格式已定义，需要后端按照此格式实现
- 前端已实现完整的错误处理和用户提示
- 支持响应式布局，适配不同屏幕尺寸

## License

MIT
