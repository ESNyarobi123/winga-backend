-- =============================================================================
-- Winga — Add filter columns to jobs (employment_type, social_media, software, language)
-- Run after v4-update.sql. Safe to run multiple times.
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v9-jobs-filter-columns.sql
-- =============================================================================

USE winga_db;

SET @db = DATABASE();

-- employment_type
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'employment_type');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN employment_type VARCHAR(100) DEFAULT NULL AFTER category', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- social_media
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'social_media');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN social_media VARCHAR(100) DEFAULT NULL AFTER employment_type', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- software
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'software');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN software VARCHAR(100) DEFAULT NULL AFTER social_media', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- language
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'jobs' AND COLUMN_NAME = 'language');
SET @sql = IF(@q = 0, 'ALTER TABLE jobs ADD COLUMN language VARCHAR(100) DEFAULT NULL AFTER software', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- End of v9-jobs-filter-columns.sql
-- =============================================================================
