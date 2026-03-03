-- =============================================================================
-- Winga — Admin profile verification (profile_verified, profile_verified_at)
-- Run after v12-worker-profile-fields.sql.
-- =============================================================================

USE winga_db;

SET @db = DATABASE();

-- users: profile_verified (admin-set badge for workers)
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_verified');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN profile_verified TINYINT(1) DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- users: profile_verified_at
SET @q = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'users' AND COLUMN_NAME = 'profile_verified_at');
SET @sql = IF(@q = 0, 'ALTER TABLE users ADD COLUMN profile_verified_at DATETIME(6) DEFAULT NULL', 'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- End of v13-profile-verified.sql
-- =============================================================================
