-- =============================================================================
-- Winga — Add updated_at to filter_options (track when admin edits)
-- Run after v15. Idempotent: skips if column already exists.
-- =============================================================================

USE winga_db;

SET @db = DATABASE();
SET @exists = (SELECT COUNT(*) FROM information_schema.COLUMNS
  WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'filter_options' AND COLUMN_NAME = 'updated_at');
SET @sql = IF(@exists = 0,
  'ALTER TABLE filter_options ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6) AFTER created_at',
  'SELECT 1');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- =============================================================================
-- End of v16-filter-options-updated-at.sql
-- =============================================================================
