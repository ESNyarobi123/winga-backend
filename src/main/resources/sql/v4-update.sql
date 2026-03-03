-- =============================================================================
-- Winga — Database UPDATE script (idempotent)
-- Run on existing DB to add any missing columns/tables. Safe to run multiple times.
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v4-update.sql
--
-- Order of scripts if starting fresh: schema-winga.sql → v1-admin-panel.sql
--   → v2-admin-login.sql → v3-payment-options.sql → v4-update.sql (this file)
--   → v5-seed-categories.sql (optional: seed job categories)
--   → v6-seed-jobs.sql (optional: seed ~20 sample jobs + demo client)
--   → v7-seed-freelancers.sql (optional: seed demo freelancers for Find Workers page)
-- =============================================================================

USE winga_db;

-- -----------------------------------------------------------------------------
-- 1. Jobs: add columns that may be missing (from Entity, not in base schema)
-- -----------------------------------------------------------------------------

-- Add column only if it does not exist (MySQL 5.7+)
SET @db = DATABASE();

-- city
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'city');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN city VARCHAR(100) DEFAULT NULL AFTER category', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- region
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'region');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN region VARCHAR(100) DEFAULT NULL AFTER city', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- latitude
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'latitude');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN latitude DECIMAL(10,7) DEFAULT NULL AFTER region', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- longitude
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'longitude');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN longitude DECIMAL(10,7) DEFAULT NULL AFTER latitude', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- attachment_urls (Entity: attachmentUrls)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'attachment_urls');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN attachment_urls TEXT DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -----------------------------------------------------------------------------
-- 2. Jobs: ensure admin/moderation columns exist (from v1; skip if already run)
-- -----------------------------------------------------------------------------

SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'moderation_status');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN moderation_status VARCHAR(20) DEFAULT ''APPROVED''', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'is_featured');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN is_featured TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'is_boosted_telegram');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN is_boosted_telegram TINYINT(1) NOT NULL DEFAULT 0', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

UPDATE jobs SET moderation_status = 'APPROVED' WHERE moderation_status IS NULL;

-- -----------------------------------------------------------------------------
-- 3. job_categories (from v1) — create if not exists
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS job_categories (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  name       VARCHAR(100) NOT NULL,
  slug       VARCHAR(100) NOT NULL,
  sort_order INT          NOT NULL DEFAULT 0,
  created_at DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY idx_job_category_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. payment_options (from v3) — create if not exists
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS payment_options (
  id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
  name        VARCHAR(100) NOT NULL,
  slug        VARCHAR(100) NOT NULL,
  description VARCHAR(500) DEFAULT NULL,
  is_active   TINYINT(1)   NOT NULL DEFAULT 1,
  sort_order  INT          NOT NULL DEFAULT 0,
  created_at  DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  UNIQUE KEY idx_payment_option_slug (slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- End of v4-update.sql
-- =============================================================================
