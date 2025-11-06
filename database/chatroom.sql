-- ============================================
-- 千语聊天室数据库表结构
-- 数据库名: chatroom
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- ============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `chatroom` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `chatroom`;

-- ============================================
-- 1. 用户表
-- ============================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` VARCHAR(50) NOT NULL COMMENT '用户名',
  `nickname` VARCHAR(100) NOT NULL COMMENT '昵称',
  `password` VARCHAR(255) NOT NULL COMMENT '密码（加密后）',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '头像URL',
  `status` ENUM('online', 'offline') DEFAULT 'offline' COMMENT '在线状态',
  `create_time` BIGINT NOT NULL COMMENT '创建时间（时间戳毫秒）',
  `update_time` BIGINT NOT NULL COMMENT '更新时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================
-- 2. 好友分组表
-- ============================================
CREATE TABLE IF NOT EXISTS `friend_groups` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '分组ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `name` VARCHAR(50) NOT NULL COMMENT '分组名称',
  `sort_order` INT DEFAULT 0 COMMENT '排序顺序',
  `create_time` BIGINT NOT NULL COMMENT '创建时间（时间戳毫秒）',
  `update_time` BIGINT NOT NULL COMMENT '更新时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_friend_groups_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友分组表';

-- ============================================
-- 3. 好友关系表
-- ============================================
CREATE TABLE IF NOT EXISTS `friends` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '关系ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `friend_id` BIGINT UNSIGNED NOT NULL COMMENT '好友ID',
  `group_id` BIGINT UNSIGNED DEFAULT NULL COMMENT '所属分组ID',
  `remark` VARCHAR(100) DEFAULT NULL COMMENT '备注名称',
  `create_time` BIGINT NOT NULL COMMENT '成为好友时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_friend` (`user_id`, `friend_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_friend_id` (`friend_id`),
  KEY `idx_group_id` (`group_id`),
  CONSTRAINT `fk_friends_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friends_friend_id` FOREIGN KEY (`friend_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friends_group_id` FOREIGN KEY (`group_id`) REFERENCES `friend_groups` (`id`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友关系表';

-- ============================================
-- 4. 好友申请表
-- ============================================
CREATE TABLE IF NOT EXISTS `friend_requests` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '申请ID',
  `applicant_id` BIGINT UNSIGNED NOT NULL COMMENT '申请人ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收人ID',
  `message` VARCHAR(500) DEFAULT NULL COMMENT '验证消息',
  `status` ENUM('pending', 'accepted', 'rejected') DEFAULT 'pending' COMMENT '申请状态',
  `create_time` BIGINT NOT NULL COMMENT '创建时间（时间戳毫秒）',
  `update_time` BIGINT NOT NULL COMMENT '更新时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_applicant_id` (`applicant_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_friend_requests_applicant_id` FOREIGN KEY (`applicant_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_friend_requests_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='好友申请表';

-- ============================================
-- 5. 群组表
-- ============================================
CREATE TABLE IF NOT EXISTS `groups` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '群组ID',
  `name` VARCHAR(100) NOT NULL COMMENT '群名称',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '群描述',
  `avatar` VARCHAR(500) DEFAULT NULL COMMENT '群头像URL',
  `owner_id` BIGINT UNSIGNED NOT NULL COMMENT '群主ID',
  `max_members` INT DEFAULT 500 COMMENT '最大成员数',
  `create_time` BIGINT NOT NULL COMMENT '创建时间（时间戳毫秒）',
  `update_time` BIGINT NOT NULL COMMENT '更新时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_owner_id` (`owner_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_groups_owner_id` FOREIGN KEY (`owner_id`) REFERENCES `users` (`id`) ON DELETE RESTRICT
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组表';

-- ============================================
-- 6. 群组成员表
-- ============================================
CREATE TABLE IF NOT EXISTS `group_members` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '成员关系ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '群组ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `role` ENUM('owner', 'admin', 'member') DEFAULT 'member' COMMENT '角色：owner群主，admin管理员，member成员',
  `nickname` VARCHAR(100) DEFAULT NULL COMMENT '群内昵称',
  `join_time` BIGINT NOT NULL COMMENT '加入时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_group_user` (`group_id`, `user_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_user_id` (`user_id`),
  CONSTRAINT `fk_group_members_group_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_members_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群组成员表';

-- ============================================
-- 7. 私聊消息表
-- ============================================
CREATE TABLE IF NOT EXISTS `private_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `receiver_id` BIGINT UNSIGNED NOT NULL COMMENT '接收者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` ENUM('text', 'image', 'file', 'audio') DEFAULT 'text' COMMENT '消息类型',
  `status` ENUM('sent', 'delivered', 'read') DEFAULT 'sent' COMMENT '消息状态',
  `timestamp` BIGINT NOT NULL COMMENT '消息时间戳（毫秒）',
  `create_time` BIGINT NOT NULL COMMENT '创建时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_receiver_id` (`receiver_id`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_sender_receiver` (`sender_id`, `receiver_id`, `timestamp`),
  CONSTRAINT `fk_private_messages_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_private_messages_receiver_id` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私聊消息表';

-- ============================================
-- 8. 群聊消息表
-- ============================================
CREATE TABLE IF NOT EXISTS `group_messages` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '消息ID',
  `group_id` BIGINT UNSIGNED NOT NULL COMMENT '群组ID',
  `sender_id` BIGINT UNSIGNED NOT NULL COMMENT '发送者ID',
  `content` TEXT NOT NULL COMMENT '消息内容',
  `message_type` ENUM('text', 'image', 'file', 'audio') DEFAULT 'text' COMMENT '消息类型',
  `timestamp` BIGINT NOT NULL COMMENT '消息时间戳（毫秒）',
  `create_time` BIGINT NOT NULL COMMENT '创建时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_sender_id` (`sender_id`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_group_timestamp` (`group_id`, `timestamp`),
  CONSTRAINT `fk_group_messages_group_id` FOREIGN KEY (`group_id`) REFERENCES `groups` (`id`) ON DELETE CASCADE,
  CONSTRAINT `fk_group_messages_sender_id` FOREIGN KEY (`sender_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='群聊消息表';

-- ============================================
-- 9. 消息已读记录表（用于群聊消息已读状态）
-- ============================================
CREATE TABLE IF NOT EXISTS `message_read_records` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `message_id` BIGINT UNSIGNED NOT NULL COMMENT '消息ID',
  `message_type` ENUM('private', 'group') NOT NULL COMMENT '消息类型',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `read_time` BIGINT NOT NULL COMMENT '已读时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_message_user` (`message_id`, `message_type`, `user_id`),
  KEY `idx_message_id` (`message_id`, `message_type`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_read_time` (`read_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息已读记录表';

-- ============================================
-- 10. 用户会话表（用于存储用户在线状态和WebSocket连接信息）
-- ============================================
CREATE TABLE IF NOT EXISTS `user_sessions` (
  `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '会话ID',
  `user_id` BIGINT UNSIGNED NOT NULL COMMENT '用户ID',
  `token` VARCHAR(500) NOT NULL COMMENT 'JWT Token',
  `device_info` VARCHAR(200) DEFAULT NULL COMMENT '设备信息',
  `ip_address` VARCHAR(50) DEFAULT NULL COMMENT 'IP地址',
  `login_time` BIGINT NOT NULL COMMENT '登录时间（时间戳毫秒）',
  `expire_time` BIGINT NOT NULL COMMENT '过期时间（时间戳毫秒）',
  `last_active_time` BIGINT NOT NULL COMMENT '最后活跃时间（时间戳毫秒）',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_token` (`token`(255)),
  KEY `idx_expire_time` (`expire_time`),
  CONSTRAINT `fk_user_sessions_user_id` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会话表';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入测试用户（密码为123456的MD5，实际使用时应该使用BCrypt或其他安全加密方式）
-- 测试账户: testuser / 123456
INSERT INTO `users` (`id`, `username`, `nickname`, `password`, `status`, `create_time`, `update_time`) VALUES
(1, 'testuser', '测试用户', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwF8xQO.q', 'offline', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(2, 'user1', '用户1', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwF8xQO.q', 'offline', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(3, 'user2', '用户2', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iwF8xQO.q', 'offline', UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- 创建默认好友分组
INSERT INTO `friend_groups` (`user_id`, `name`, `sort_order`, `create_time`, `update_time`) VALUES
(1, '我的好友', 1, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(2, '我的好友', 1, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
(3, '我的好友', 1, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- 创建测试好友关系
INSERT INTO `friends` (`user_id`, `friend_id`, `group_id`, `create_time`) VALUES
(1, 2, 1, UNIX_TIMESTAMP(NOW()) * 1000),
(2, 1, 1, UNIX_TIMESTAMP(NOW()) * 1000);

-- 创建测试群组
INSERT INTO `groups` (`name`, `description`, `owner_id`, `create_time`, `update_time`) VALUES
('技术交流群', '技术讨论', 1, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000),
('闲聊群', '日常聊天', 1, UNIX_TIMESTAMP(NOW()) * 1000, UNIX_TIMESTAMP(NOW()) * 1000);

-- 添加群成员
INSERT INTO `group_members` (`group_id`, `user_id`, `role`, `join_time`) VALUES
(1, 1, 'owner', UNIX_TIMESTAMP(NOW()) * 1000),
(1, 2, 'member', UNIX_TIMESTAMP(NOW()) * 1000),
(1, 3, 'member', UNIX_TIMESTAMP(NOW()) * 1000),
(2, 1, 'owner', UNIX_TIMESTAMP(NOW()) * 1000),
(2, 2, 'member', UNIX_TIMESTAMP(NOW()) * 1000);

-- ============================================
-- 视图：好友列表视图（包含好友信息和分组信息）
-- ============================================
CREATE OR REPLACE VIEW `v_friend_list` AS
SELECT 
    f.id AS friend_relation_id,
    f.user_id,
    f.friend_id,
    f.group_id,
    f.remark,
    u.id AS friend_user_id,
    u.username AS friend_username,
    u.nickname AS friend_nickname,
    u.avatar AS friend_avatar,
    u.status AS friend_status,
    fg.name AS group_name,
    f.create_time
FROM 
    friends f
    INNER JOIN users u ON f.friend_id = u.id
    LEFT JOIN friend_groups fg ON f.group_id = fg.id;

-- ============================================
-- 视图：群组列表视图（包含群主信息和成员数）
-- ============================================
CREATE OR REPLACE VIEW `v_group_list` AS
SELECT 
    g.id,
    g.name,
    g.description,
    g.avatar,
    g.owner_id,
    u.nickname AS owner_nickname,
    COUNT(gm.id) AS member_count,
    g.create_time,
    g.update_time
FROM 
    groups g
    INNER JOIN users u ON g.owner_id = u.id
    LEFT JOIN group_members gm ON g.id = gm.group_id
GROUP BY 
    g.id, g.name, g.description, g.avatar, g.owner_id, u.nickname, g.create_time, g.update_time;

-- ============================================
-- 存储过程：获取未读消息数
-- ============================================
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS `sp_get_unread_count`(IN p_user_id BIGINT)
BEGIN
    -- 获取私聊未读消息数
    SELECT 
        receiver_id AS chat_id,
        'friend' AS chat_type,
        COUNT(*) AS unread_count
    FROM 
        private_messages
    WHERE 
        receiver_id = p_user_id
        AND status != 'read'
    GROUP BY 
        receiver_id
    
    UNION ALL
    
    -- 获取群聊未读消息数（用户未读的群消息）
    SELECT 
        gm.group_id AS chat_id,
        'group' AS chat_type,
        COUNT(*) AS unread_count
    FROM 
        group_messages gm
        INNER JOIN group_members gmem ON gm.group_id = gmem.group_id
        LEFT JOIN message_read_records mrr ON gm.id = mrr.message_id 
            AND mrr.message_type = 'group' 
            AND mrr.user_id = p_user_id
    WHERE 
        gmem.user_id = p_user_id
        AND gm.sender_id != p_user_id
        AND mrr.id IS NULL
    GROUP BY 
        gm.group_id;
END$$

DELIMITER ;

-- ============================================
-- 触发器：自动更新群组成员数（可选，如果使用视图则不需要）
-- ============================================
-- 注意：实际项目中建议使用视图或应用层计算，触发器会影响性能

-- ============================================
-- 索引优化建议
-- ============================================
-- 1. 消息表已创建联合索引用于查询优化
-- 2. 好友关系表已创建唯一索引防止重复
-- 3. 时间戳字段已创建索引用于排序和分页
-- 4. 建议定期清理过期会话数据

-- ============================================
-- 数据清理脚本（可选，用于定期清理）
-- ============================================
-- 清理过期会话
-- DELETE FROM user_sessions WHERE expire_time < UNIX_TIMESTAMP(NOW()) * 1000;

-- 清理已读消息记录（保留最近3个月）
-- DELETE FROM message_read_records WHERE read_time < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 3 MONTH)) * 1000;

-- 清理旧消息（保留最近1年）
-- DELETE FROM private_messages WHERE create_time < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 YEAR)) * 1000;
-- DELETE FROM group_messages WHERE create_time < UNIX_TIMESTAMP(DATE_SUB(NOW(), INTERVAL 1 YEAR)) * 1000;

