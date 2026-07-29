-- ============================================================
-- 用户Agent（预设提示词）功能数据库迁移
-- ============================================================

-- 1. 用户自定义Agent表
CREATE TABLE IF NOT EXISTS `user_agents` (
    `id`            BIGINT       NOT NULL COMMENT '雪花ID',
    `user_id`       BIGINT       NOT NULL COMMENT '所属用户ID，关联users.user_id',
    `name`          VARCHAR(20)  NOT NULL COMMENT 'Agent名称（最多20字符）',
    `prompt`        TEXT         NOT NULL COMMENT '提示词内容',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DELETED',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_agents_user_id` (`user_id`),
    INDEX `idx_user_agents_user_name` (`user_id`, `name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户自定义Agent（预设提示词）';

-- 2. 公共Agent模板表
CREATE TABLE IF NOT EXISTS `public_agent_templates` (
    `id`            BIGINT       NOT NULL COMMENT '雪花ID',
    `name`          VARCHAR(50)  NOT NULL COMMENT '模板名称',
    `category`      VARCHAR(50)  DEFAULT NULL COMMENT '模板分类',
    `prompt`        TEXT         NOT NULL COMMENT '提示词模板内容',
    `status`        VARCHAR(16)  NOT NULL DEFAULT 'ACTIVE' COMMENT '状态：ACTIVE/DISABLED',
    `created_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_public_templates_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='公共Agent模板（所有用户可见）';

-- ============================================================
-- 3. 预设公共模板数据
-- ============================================================

-- 写邮件模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10001, '写邮件', '办公', '我需要写一封邮件给 [收件人]，目的是 [提出什么请求]。背景原因是 [说明理由]。我希望这封邮件能 [表达出专业/恳切/理直气壮] 的感觉。请在邮件中明确提到：[关键数据/附件/截止日期]。请给我两个版本：一个非常简洁，一个稍微详细一些。', 'ACTIVE');

-- 会议纪要模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10002, '会议纪要', '办公', '请根据以下会议内容生成一份结构清晰的会议纪要，包括：会议主题、参会人员、讨论要点、决议事项、待办任务及负责人。\n\n会议内容：[请在此输入会议内容]', 'ACTIVE');

-- 周报模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10003, '周报', '办公', '请根据以下本周工作内容，帮我生成一份专业的周报。格式要求：\n1. 本周完成工作（分点列出）\n2. 下周工作计划（分点列出）\n3. 遇到的问题及风险\n4. 需要协助的事项\n\n本周工作内容：[请在此输入工作内容]', 'ACTIVE');

-- 需求分析模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10004, '需求分析', '技术', '请帮我分析以下需求，并提供结构化的需求文档框架：\n1. 需求背景\n2. 功能描述\n3. 用户场景\n4. 验收标准\n5. 技术可行性分析\n\n需求描述：[请在此输入需求描述]', 'ACTIVE');

-- 代码审查模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10005, '代码审查', '技术', '请对以下代码进行审查，关注以下方面：\n1. 代码结构和可读性\n2. 潜在的性能问题\n3. 安全漏洞\n4. 边界条件处理\n5. 改进建议\n\n代码内容：[请在此粘贴代码]', 'ACTIVE');

-- 翻译助手模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10006, '翻译助手', '通用', '请将以下内容翻译成 [目标语言]，要求：\n- 保持原意准确\n- 符合目标语言的表达习惯\n- 专业术语需准确翻译\n- 如有文化特定词汇，请添加注释说明\n\n待翻译内容：[请在此输入需要翻译的内容]', 'ACTIVE');

-- 文档总结模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10007, '文档总结', '通用', '请对以下文档内容进行总结提炼，要求：\n1. 核心要点（3-5条）\n2. 关键数据和结论\n3. 存在的疑问或待确认事项\n\n文档内容：[请在此输入文档内容]', 'ACTIVE');

-- 头脑风暴模板
INSERT INTO `public_agent_templates` (`id`, `name`, `category`, `prompt`, `status`) VALUES
(10008, '头脑风暴', '创意', '关于主题「[主题]」，请帮我进行头脑风暴，从以下角度展开：\n1. 现状分析\n2. 机会点\n3. 潜在风险\n4. 创新思路（至少5条）\n5. 可行性排序\n\n请提供具体、可落地的建议。', 'ACTIVE');
