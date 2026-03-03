-- =============================================================================
-- Winga — Saved workers (bookmark freelancers) table
-- Run after schema. Safe to run (CREATE TABLE IF NOT EXISTS).
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v10-saved-workers.sql
-- =============================================================================

USE winga_db;

CREATE TABLE IF NOT EXISTS saved_workers (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  user_id    BIGINT       NOT NULL,
  worker_id  BIGINT       NOT NULL,
  created_at DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  KEY idx_saved_worker_user (user_id),
  KEY idx_saved_worker_worker (worker_id),
  UNIQUE KEY uq_saved_user_worker (user_id, worker_id),
  CONSTRAINT fk_saved_worker_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_saved_worker_worker FOREIGN KEY (worker_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- End of v10-saved-workers.sql
-- =============================================================================
