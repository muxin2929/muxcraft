-- ============================================================
-- MuXCraft 数据库初始化脚本
-- 生成时间: 2026-10-13
-- 说明: 创建整个数据库及所有表结构，包含完整的备注信息
-- ============================================================

CREATE DATABASE IF NOT EXISTS `muxcraft`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

USE `muxcraft`;

-- ------------------------------------------------------------
-- 1. users - 用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `user_id`     varchar(32)  NOT NULL COMMENT '登录账号',
  `nickname`    varchar(32)  NOT NULL COMMENT '昵称',
  `password`    varchar(100) NOT NULL COMMENT 'BCrypt 加密密码',
  `avatar_url`  varchar(255) DEFAULT NULL COMMENT '头像地址',
  `role`        varchar(16)  NOT NULL DEFAULT 'USER' COMMENT 'USER / ADMIN',
  `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';

-- ------------------------------------------------------------
-- 2. skills - 技术栈表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `skills`;
CREATE TABLE `skills` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `name`        varchar(64)  NOT NULL COMMENT '技术名称',
  `category`    varchar(32)  NOT NULL COMMENT '分类：Frontend / Backend / Tool 等',
  `icon_url`    varchar(255) DEFAULT NULL COMMENT '图标地址',
  `rating`      tinyint      NOT NULL DEFAULT '3' COMMENT '熟练度 1-5',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `sort_order`  int          NOT NULL DEFAULT '0' COMMENT '排序',
  `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  PRIMARY KEY (`id`),
  KEY `idx_category_status` (`category`, `status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='技术栈';

-- ------------------------------------------------------------
-- 3. projects - 项目表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `projects`;
CREATE TABLE `projects` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `name`        varchar(128) NOT NULL COMMENT '项目名称',
  `slug`        varchar(64)  NOT NULL COMMENT '项目标识，与 GitHub 仓库同名',
  `summary`     varchar(255) DEFAULT NULL COMMENT '摘要',
  `description` text         COMMENT '详细描述',
  `cover_url`   varchar(255) DEFAULT NULL COMMENT '封面图地址',
  `github_url`  varchar(255) DEFAULT NULL COMMENT 'GitHub 地址',
  `demo_url`    varchar(255) DEFAULT NULL COMMENT 'GitHub Pages 静态页',
  `status`      tinyint      NOT NULL DEFAULT '1' COMMENT '1启用 0禁用',
  `sort_order`  int          NOT NULL DEFAULT '0' COMMENT '排序',
  `created_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_project_slug` (`slug`),
  KEY `idx_status_sort` (`status`, `sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目';

-- ------------------------------------------------------------
-- 4. project_skills - 项目-技术栈关联表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `project_skills`;
CREATE TABLE `project_skills` (
  `project_id` bigint NOT NULL COMMENT '项目id',
  `skill_id`   bigint NOT NULL COMMENT '技术栈id',
  PRIMARY KEY (`project_id`, `skill_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目技术栈关联';

-- ------------------------------------------------------------
-- 5. articles - 博客文章表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `articles`;
CREATE TABLE `articles` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `title`        varchar(255) NOT NULL COMMENT '标题',
  `slug`         varchar(128) NOT NULL COMMENT '文章标识',
  `summary`      varchar(500) DEFAULT NULL COMMENT '摘要',
  `cover_url`    varchar(255) DEFAULT NULL COMMENT '封面图地址',
  `status`       varchar(16)  NOT NULL DEFAULT 'DRAFT' COMMENT '状态：DRAFT / PUBLISHED',
  `published_at` datetime     DEFAULT NULL COMMENT '发布时间',
  `created_at`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_article_slug` (`slug`),
  KEY `idx_status_published` (`status`, `published_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='博客文章';

-- ------------------------------------------------------------
-- 6. comments - 项目评论表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `comments`;
CREATE TABLE `comments` (
  `id`          bigint   NOT NULL AUTO_INCREMENT,
  `project_id`  bigint   NOT NULL COMMENT '项目id',
  `user_id`     bigint   NOT NULL COMMENT '评论用户id',
  `reply_to_id` bigint   DEFAULT NULL COMMENT '被回复的评论id',
  `content`     text     NOT NULL COMMENT '评论内容',
  `created_at`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_project_created` (`project_id`, `created_at`),
  KEY `idx_user` (`user_id`),
  KEY `idx_reply_to` (`reply_to_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='项目评论';

-- ------------------------------------------------------------
-- 7. guestbook - 留言板表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `guestbook`;
CREATE TABLE `guestbook` (
  `id`          bigint   NOT NULL AUTO_INCREMENT,
  `user_id`     bigint   NOT NULL COMMENT '留言用户id',
  `reply_to_id` bigint   DEFAULT NULL COMMENT '被回复的留言id',
  `content`     text     NOT NULL COMMENT '留言内容',
  `created_at`  datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_created` (`created_at`),
  KEY `idx_reply_to` (`reply_to_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='留言板';

-- ------------------------------------------------------------
-- 8. suggestions - 用户建议表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `suggestions`;
CREATE TABLE `suggestions` (
  `id`         bigint       NOT NULL AUTO_INCREMENT,
  `user_id`    bigint       NOT NULL COMMENT '提交建议的用户id',
  `title`      varchar(128) NOT NULL COMMENT '建议标题',
  `content`    text         NOT NULL COMMENT '建议内容',
  `status`     varchar(16)  NOT NULL DEFAULT 'UNREAD' COMMENT '状态：UNREAD / READ / ARCHIVED',
  `created_at` datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_status_created` (`status`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户建议';

-- ------------------------------------------------------------
-- 9. achievements - 成就/里程碑表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `achievements`;
CREATE TABLE `achievements` (
  `id`          bigint       NOT NULL AUTO_INCREMENT,
  `year`        varchar(8)   NOT NULL COMMENT '年份',
  `title`       varchar(255) NOT NULL COMMENT '标题',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort_order`  int          NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='成就';

-- ------------------------------------------------------------
-- 10. experiences - 经历/旅程表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `experiences`;
CREATE TABLE `experiences` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `title`        varchar(128) NOT NULL COMMENT '标题',
  `organization` varchar(128) DEFAULT NULL COMMENT '组织/公司',
  `description`  text         COMMENT '描述',
  `cover_url`    varchar(255) DEFAULT NULL COMMENT '封面图地址',
  `page_url`     varchar(255) DEFAULT NULL COMMENT 'Hexo/GitHub Pages 静态页',
  `start_date`   date         DEFAULT NULL COMMENT '开始日期',
  `end_date`     date         DEFAULT NULL COMMENT '结束日期',
  `sort_order`   int          NOT NULL DEFAULT '0' COMMENT '排序',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='经历/旅程';

-- ------------------------------------------------------------
-- 11. site_settings - 站点设置表（单行）
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `site_settings`;
CREATE TABLE `site_settings` (
  `id`                   tinyint      NOT NULL DEFAULT '1' COMMENT '固定为 1，单行数据',
  `site_title`           varchar(64)  NOT NULL DEFAULT 'MuXCraft' COMMENT '站点标题',
  `site_description`     varchar(255) DEFAULT NULL COMMENT '站点描述',
  `profile_name`         varchar(64)  DEFAULT NULL COMMENT '个人名称',
  `profile_title`        varchar(128) DEFAULT NULL COMMENT '个人职位/头衔',
  `short_intro`          text         COMMENT '简短介绍',
  `full_intro`           text         COMMENT '完整介绍',
  `location`             varchar(128) DEFAULT NULL COMMENT '所在地',
  `photo_url`            varchar(255) DEFAULT NULL COMMENT '个人照片地址',
  `github_url`           varchar(255) DEFAULT NULL COMMENT 'GitHub 地址',
  `website_url`          varchar(255) DEFAULT NULL COMMENT '个人网站地址',
  `registration_enabled` tinyint      NOT NULL DEFAULT '1' COMMENT '是否开放注册：1开启 0关闭',
  `guestbook_enabled`    tinyint      NOT NULL DEFAULT '1' COMMENT '是否开启留言板：1开启 0关闭',
  `comment_enabled`      tinyint      NOT NULL DEFAULT '1' COMMENT '是否开启评论：1开启 0关闭',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='站点设置（单行）';

-- ------------------------------------------------------------
-- 12. user_block - 屏蔽用户表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `user_block`;
CREATE TABLE `user_block` (
  `id`         bigint   NOT NULL AUTO_INCREMENT,
  `blocker_id` bigint   NOT NULL COMMENT '屏蔽发起人（我）',
  `blocked_id` bigint   NOT NULL COMMENT '被屏蔽的用户id',
  `created_at` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_block` (`blocker_id`, `blocked_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='屏蔽用户';

-- ------------------------------------------------------------
-- 13. chat_message - 私信消息表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `chat_message`;
CREATE TABLE `chat_message` (
  `id`           bigint       NOT NULL AUTO_INCREMENT,
  `from_user_id` bigint       NOT NULL COMMENT '发送者id',
  `to_user_id`   bigint       NOT NULL COMMENT '接收者id',
  `content`      varchar(1000) NOT NULL COMMENT '消息内容',
  `created_at`   datetime     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read`      tinyint      DEFAULT '0' COMMENT '是否已读：0未读 1已读',
  PRIMARY KEY (`id`),
  KEY `idx_from_to` (`from_user_id`, `to_user_id`, `created_at`),
  KEY `idx_to_from` (`to_user_id`, `from_user_id`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='私信消息';

-- ------------------------------------------------------------
-- 14. chat_conversation - 会话状态表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `chat_conversation`;
CREATE TABLE `chat_conversation` (
  `user_id`         bigint   NOT NULL COMMENT '当前用户id',
  `other_user_id`   bigint   NOT NULL COMMENT '对方用户id',
  `hidden`          tinyint  NOT NULL DEFAULT '0' COMMENT '是否对当前用户隐藏（删除会话）：0否 1是',
  `pinned`          tinyint  NOT NULL DEFAULT '0' COMMENT '是否置顶：0否 1是',
  `last_message_id` bigint   DEFAULT NULL COMMENT '最近一条消息id',
  `last_at`         datetime DEFAULT NULL COMMENT '最近消息时间',
  PRIMARY KEY (`user_id`, `other_user_id`),
  KEY `idx_user_last` (`user_id`, `pinned`, `last_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会话状态';

-- ------------------------------------------------------------
-- 15. chat_message_deleted - 消息单边隐藏记录表
-- ------------------------------------------------------------
DROP TABLE IF EXISTS `chat_message_deleted`;
CREATE TABLE `chat_message_deleted` (
  `message_id` bigint NOT NULL COMMENT '被隐藏的消息id',
  `user_id`    bigint NOT NULL COMMENT '隐藏该消息的用户id',
  PRIMARY KEY (`message_id`, `user_id`),
  KEY `idx_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息单边隐藏记录';

-- ============================================================
-- 初始化数据
-- ============================================================

-- 初始化站点设置（单行，id 固定为 1）
INSERT INTO `site_settings`
  (`id`, `site_title`, `site_description`, `profile_name`, `profile_title`,
   `short_intro`, `full_intro`, `location`, `photo_url`, `github_url`,
   `website_url`, `registration_enabled`, `guestbook_enabled`, `comment_enabled`)
VALUES
  (1, 'MuXCraft', 'MuXCraft 数字工坊', 'Muxin', '全栈开发者',
   '热爱编程与创造', '欢迎来到 MuXCraft 数字工坊', NULL, NULL, 'https://github.com',
   'https://muxcraft.com', 1, 1, 1);

-- 初始化默认管理员账号
-- 登录账号: admin  密码: admin123
INSERT INTO `users`
  (`user_id`, `nickname`, `password`, `avatar_url`, `role`, `status`)
VALUES
  ('admin', 'Admin', '$2b$10$C422bRpMftBK6uHmnEr7.OZUY/4ozYXGP4yYwHvxQuimrZTAyDgfG', NULL, 'ADMIN', 1);