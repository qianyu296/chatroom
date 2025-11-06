# 千语聊天室数据库设计文档

## 数据库说明

本SQL文件包含了千语聊天室项目的完整数据库表结构，基于API_DOC.md中的接口需求设计。

## 数据库信息

- **数据库名**: `chatroom`
- **字符集**: `utf8mb4`
- **排序规则**: `utf8mb4_unicode_ci`
- **存储引擎**: `InnoDB`

## 表结构说明

### 1. 用户表 (users)
存储用户基本信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 用户ID（主键） |
| username | VARCHAR(50) | 用户名（唯一） |
| nickname | VARCHAR(100) | 昵称 |
| password | VARCHAR(255) | 密码（加密后） |
| avatar | VARCHAR(500) | 头像URL |
| status | ENUM | 在线状态（online/offline） |
| create_time | BIGINT | 创建时间（时间戳毫秒） |
| update_time | BIGINT | 更新时间（时间戳毫秒） |

### 2. 好友分组表 (friend_groups)
存储用户的好友分组信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 分组ID（主键） |
| user_id | BIGINT | 用户ID（外键） |
| name | VARCHAR(50) | 分组名称 |
| sort_order | INT | 排序顺序 |
| create_time | BIGINT | 创建时间 |
| update_time | BIGINT | 更新时间 |

### 3. 好友关系表 (friends)
存储用户之间的好友关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 关系ID（主键） |
| user_id | BIGINT | 用户ID（外键） |
| friend_id | BIGINT | 好友ID（外键） |
| group_id | BIGINT | 所属分组ID（外键，可为空） |
| remark | VARCHAR(100) | 备注名称 |
| create_time | BIGINT | 成为好友时间 |

**注意**: 好友关系是双向的，需要插入两条记录（A->B 和 B->A）

### 4. 好友申请表 (friend_requests)
存储好友申请记录

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 申请ID（主键） |
| applicant_id | BIGINT | 申请人ID（外键） |
| receiver_id | BIGINT | 接收人ID（外键） |
| message | VARCHAR(500) | 验证消息 |
| status | ENUM | 申请状态（pending/accepted/rejected） |
| create_time | BIGINT | 创建时间 |
| update_time | BIGINT | 更新时间 |

### 5. 群组表 (groups)
存储群组基本信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 群组ID（主键） |
| name | VARCHAR(100) | 群名称 |
| description | VARCHAR(500) | 群描述 |
| avatar | VARCHAR(500) | 群头像URL |
| owner_id | BIGINT | 群主ID（外键） |
| max_members | INT | 最大成员数（默认500） |
| create_time | BIGINT | 创建时间 |
| update_time | BIGINT | 更新时间 |

### 6. 群组成员表 (group_members)
存储群组成员关系

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 成员关系ID（主键） |
| group_id | BIGINT | 群组ID（外键） |
| user_id | BIGINT | 用户ID（外键） |
| role | ENUM | 角色（owner/admin/member） |
| nickname | VARCHAR(100) | 群内昵称 |
| join_time | BIGINT | 加入时间 |

### 7. 私聊消息表 (private_messages)
存储私聊消息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 消息ID（主键） |
| sender_id | BIGINT | 发送者ID（外键） |
| receiver_id | BIGINT | 接收者ID（外键） |
| content | TEXT | 消息内容 |
| message_type | ENUM | 消息类型（text/image/file/audio） |
| status | ENUM | 消息状态（sent/delivered/read） |
| timestamp | BIGINT | 消息时间戳（毫秒） |
| create_time | BIGINT | 创建时间 |

### 8. 群聊消息表 (group_messages)
存储群聊消息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 消息ID（主键） |
| group_id | BIGINT | 群组ID（外键） |
| sender_id | BIGINT | 发送者ID（外键） |
| content | TEXT | 消息内容 |
| message_type | ENUM | 消息类型（text/image/file/audio） |
| timestamp | BIGINT | 消息时间戳（毫秒） |
| create_time | BIGINT | 创建时间 |

### 9. 消息已读记录表 (message_read_records)
存储消息已读状态（主要用于群聊）

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 记录ID（主键） |
| message_id | BIGINT | 消息ID |
| message_type | ENUM | 消息类型（private/group） |
| user_id | BIGINT | 用户ID（外键） |
| read_time | BIGINT | 已读时间 |

### 10. 用户会话表 (user_sessions)
存储用户登录会话信息

| 字段 | 类型 | 说明 |
|------|------|------|
| id | BIGINT | 会话ID（主键） |
| user_id | BIGINT | 用户ID（外键） |
| token | VARCHAR(500) | JWT Token |
| device_info | VARCHAR(200) | 设备信息 |
| ip_address | VARCHAR(50) | IP地址 |
| login_time | BIGINT | 登录时间 |
| expire_time | BIGINT | 过期时间 |
| last_active_time | BIGINT | 最后活跃时间 |

## 视图说明

### 1. v_friend_list
好友列表视图，包含好友信息和分组信息，方便查询。

### 2. v_group_list
群组列表视图，包含群主信息和成员数统计。

## 存储过程说明

### sp_get_unread_count
获取用户未读消息数（私聊和群聊）

**参数**:
- `p_user_id`: 用户ID

**返回**: 包含chat_id、chat_type和unread_count的结果集

## 使用说明

### 1. 导入数据库

```bash
# 方式1：使用mysql命令行
mysql -u root -p < database/chatroom.sql

# 方式2：使用source命令
mysql -u root -p
mysql> source database/chatroom.sql;
```

### 2. 测试账户

SQL文件中已包含测试数据：

- **测试用户1**: testuser / 123456
- **测试用户2**: user1 / 123456
- **测试用户3**: user2 / 123456

**注意**: 密码使用了BCrypt加密（示例密码为123456的加密结果）。实际使用时应该使用Spring Security的BCryptPasswordEncoder进行密码加密和验证。

### 3. 索引优化

所有表已创建必要的索引：
- 主键索引
- 唯一索引（防止重复数据）
- 外键索引（优化关联查询）
- 时间戳索引（优化排序和分页）
- 联合索引（优化复杂查询）

### 4. 数据清理建议

建议定期执行以下清理操作：

1. **清理过期会话**: 删除过期的用户会话记录
2. **清理已读记录**: 删除3个月前的已读记录
3. **归档旧消息**: 将1年前的消息归档或删除

## 注意事项

1. **时间戳**: 所有时间字段使用BIGINT类型存储Unix时间戳（毫秒）
2. **字符集**: 使用utf8mb4支持emoji等特殊字符
3. **外键约束**: 已设置外键约束保证数据一致性
4. **级联删除**: 删除用户时会级联删除相关的好友关系、消息等
5. **密码加密**: 密码必须使用BCrypt等安全加密方式存储
6. **索引维护**: 定期检查索引使用情况，根据实际查询优化索引

## 扩展建议

1. **分表策略**: 消息表数据量大时，建议按时间或用户ID分表
2. **读写分离**: 消息查询频繁时，考虑读写分离
3. **缓存策略**: 使用Redis缓存热点数据（好友列表、群组信息等）
4. **消息队列**: 使用消息队列处理消息发送，提高并发性能
5. **全文搜索**: 如需消息搜索功能，可考虑使用Elasticsearch

## 版本历史

- **v1.0** (2024): 初始版本，包含基础表结构和视图

