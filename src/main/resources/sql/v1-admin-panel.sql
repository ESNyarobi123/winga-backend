-- Admin panel & OFM-style features (run after schema-winga.sql)
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v1-admin-panel.sql

USE winga_db;

-- Job moderation & boost (run once; if columns exist, ignore errors)
ALTER TABLE jobs ADD COLUMN moderation_status VARCHAR(20) DEFAULT 'APPROVED';
ALTER TABLE jobs ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0;
ALTER TABLE jobs ADD COLUMN is_boosted_telegram TINYINT(1) NOT NULL DEFAULT 0;
UPDATE jobs SET moderation_status = 'APPROVED' WHERE moderation_status IS NULL;

-- Job categories (OFM: OnlyFans Chatter, VA, Editor, Telegram Closer, etc.)
CREATE TABLE IF NOT EXISTS job_categories (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  slug       VARCHAR(100) NOT NULL,
  sort_order INT          NOT NULL DEFAULT 0,
  created_at DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY idx_job_category_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed OFM-style categories
INSERT IGNORE INTO job_categories (name, slug, sort_order, created_at) VALUES
  ('OnlyFans Chatter', 'onlyfans-chatter', 1, NOW()),
  ('Virtual Assistant', 'virtual-assistant', 2, NOW()),
  ('Content Editor', 'content-editor', 3, NOW()),
  ('Telegram Closer', 'telegram-closer', 4, NOW()),
  ('OnlyFans Chatter Trainer', 'onlyfans-chatter-trainer', 5, NOW()),
  ('Social Media Manager', 'social-media-manager', 6, NOW());
