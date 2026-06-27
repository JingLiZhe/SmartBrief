CREATE TABLE IF NOT EXISTS `home_news` (
  `id` BIGINT AUTO_INCREMENT PRIMARY KEY,
  `news_id` VARCHAR(64) NOT NULL UNIQUE COMMENT '新闻唯一标识',
  `title` VARCHAR(500) NOT NULL COMMENT '新闻标题',
  `description` TEXT COMMENT '新闻描述',
  `source` VARCHAR(100) COMMENT '新闻来源',
  `pic_url` VARCHAR(500) COMMENT '图片URL',
  `url` VARCHAR(500) COMMENT '新闻详情链接',
  `ctime` DATETIME COMMENT '新闻发布时间',
  `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  INDEX `idx_ctime` (`ctime`),
  INDEX `idx_news_id` (`news_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='首页新闻表';