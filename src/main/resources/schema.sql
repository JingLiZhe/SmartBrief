-- 用户表添加头像字段
DELIMITER //
CREATE PROCEDURE AddAvatarColumn()
BEGIN
    DECLARE col_count INT DEFAULT 0;
    SELECT COUNT(*) INTO col_count 
    FROM INFORMATION_SCHEMA.COLUMNS 
    WHERE table_schema = DATABASE() 
    AND table_name = 'user' 
    AND column_name = 'avatar';
    IF col_count = 0 THEN
        ALTER TABLE user ADD COLUMN avatar VARCHAR(255) NULL;
    END IF;
END //
DELIMITER ;
CALL AddAvatarColumn();
DROP PROCEDURE AddAvatarColumn;

-- 用户文本表
CREATE TABLE IF NOT EXISTS user_text (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
);

-- 搜索历史表
CREATE TABLE IF NOT EXISTS search_history (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    keyword VARCHAR(500) NOT NULL,
    result TEXT,
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_create_time (create_time)
);

-- 总结表（更新）
DROP TABLE IF EXISTS summary;
CREATE TABLE IF NOT EXISTS summary (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    source_type VARCHAR(50) NOT NULL COMMENT '来源类型：TEXT, NEWS等',
    source_id BIGINT COMMENT '来源ID',
    content TEXT NOT NULL COMMENT '总结内容',
    language VARCHAR(10) DEFAULT 'zh-CN',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_source_type (source_type),
    INDEX idx_create_time (create_time)
);
