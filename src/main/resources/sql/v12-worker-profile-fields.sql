-- =============================================================================
-- Winga — Worker profile fields (headline, country_code, type_speed, etc.)
-- and work_experiences.skills_learned. Run after v4-update.sql.
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v12-worker-profile-fields.sql
-- =============================================================================

USE winga_db;

SET @db = DATABASE();

-- users: headline (✅ required for complete worker profile)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'headline');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN headline VARCHAR(500) DEFAULT NULL AFTER country', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users: country_code (for flag)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'country_code');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN country_code VARCHAR(10) DEFAULT NULL AFTER headline', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users: type_speed (optional)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'type_speed');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN type_speed VARCHAR(50) DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users: internet_speed (optional)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'internet_speed');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN internet_speed VARCHAR(50) DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users: computer_specs (optional)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'computer_specs');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN computer_specs TEXT DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users: has_webcam (optional)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'has_webcam');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN has_webcam TINYINT(1) DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- work_experiences: skills_learned (JSON array of tags)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'work_experiences' AND COLUMN_NAME = 'skills_learned');
SET @sql = IF(@q = 0, 'ALTER TABLE work_experiences ADD COLUMN skills_learned TEXT DEFAULT NULL AFTER description', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- End of v12-worker-profile-fields.sql
-- =============================================================================
