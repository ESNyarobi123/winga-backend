-- =============================================================================
-- Winga — Filter options table + seed (Employment Type, Social Media, Software, Languages)
-- Run after v4-update.sql. Safe to run multiple times (INSERT IGNORE on type+slug).
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v8-filter-options.sql
-- =============================================================================

USE winga_db;

-- -----------------------------------------------------------------------------
-- 1. filter_options table
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS filter_options (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  type       VARCHAR(30)  NOT NULL,
  name       VARCHAR(100) NOT NULL,
  slug       VARCHAR(100) NOT NULL,
  sort_order INT          NOT NULL DEFAULT 0,
  created_at DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  KEY idx_filter_option_type (type),
  UNIQUE KEY uq_filter_option_type_slug (type, slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. Seed data (idempotent: duplicate type+slug skipped)
-- -----------------------------------------------------------------------------

-- Employment Type
INSERT IGNORE INTO filter_options (type, name, slug, sort_order, created_at) VALUES
  ('EMPLOYMENT_TYPE', 'Full-time', 'full-time', 1, NOW()),
  ('EMPLOYMENT_TYPE', 'Part-time', 'part-time', 2, NOW()),
  ('EMPLOYMENT_TYPE', 'Contract', 'contract', 3, NOW()),
  ('EMPLOYMENT_TYPE', 'Freelance', 'freelance', 4, NOW()),
  ('EMPLOYMENT_TYPE', 'Temporary', 'temporary', 5, NOW());

-- Social Media
INSERT IGNORE INTO filter_options (type, name, slug, sort_order, created_at) VALUES
  ('SOCIAL_MEDIA', 'Instagram', 'instagram', 1, NOW()),
  ('SOCIAL_MEDIA', 'TikTok', 'tiktok', 2, NOW()),
  ('SOCIAL_MEDIA', 'OnlyFans', 'onlyfans', 3, NOW()),
  ('SOCIAL_MEDIA', 'Facebook', 'facebook', 4, NOW()),
  ('SOCIAL_MEDIA', 'Twitter / X', 'twitter-x', 5, NOW()),
  ('SOCIAL_MEDIA', 'YouTube', 'youtube', 6, NOW()),
  ('SOCIAL_MEDIA', 'Telegram', 'telegram', 7, NOW());

-- Software
INSERT IGNORE INTO filter_options (type, name, slug, sort_order, created_at) VALUES
  ('SOFTWARE', 'Microsoft Excel', 'excel', 1, NOW()),
  ('SOFTWARE', 'Google Docs', 'google-docs', 2, NOW()),
  ('SOFTWARE', 'Photoshop', 'photoshop', 3, NOW()),
  ('SOFTWARE', 'Canva', 'canva', 4, NOW()),
  ('SOFTWARE', 'Slack', 'slack', 5, NOW()),
  ('SOFTWARE', 'Zoom', 'zoom', 6, NOW()),
  ('SOFTWARE', 'Figma', 'figma', 7, NOW()),
  ('SOFTWARE', 'VS Code', 'vs-code', 8, NOW());

-- Languages
INSERT IGNORE INTO filter_options (type, name, slug, sort_order, created_at) VALUES
  ('LANGUAGE', 'English', 'english', 1, NOW()),
  ('LANGUAGE', 'Swahili', 'swahili', 2, NOW()),
  ('LANGUAGE', 'French', 'french', 3, NOW()),
  ('LANGUAGE', 'Arabic', 'arabic', 4, NOW()),
  ('LANGUAGE', 'Spanish', 'spanish', 5, NOW()),
  ('LANGUAGE', 'Portuguese', 'portuguese', 6, NOW()),
  ('LANGUAGE', 'German', 'german', 7, NOW());

-- =============================================================================
-- End of v8-filter-options.sql
-- =============================================================================
