-- =============================================================================
-- Winga — Qualification tests (admin-managed) + worker results (completed → profile)
-- Run after v16. Tests displayed on worker my-tests; when complete, add to profile.
-- =============================================================================

USE winga_db;

-- Tests defined by admin (min/max score, max attempts, active)
CREATE TABLE IF NOT EXISTS qualification_tests (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  name          VARCHAR(150) NOT NULL,
  slug          VARCHAR(100) NOT NULL,
  test_type     VARCHAR(50)  NOT NULL COMMENT 'TYPING, ENGLISH_B1, ENGLISH_B2, INTERNET_SPEED, VERBAL, etc.',
  min_score     INT          NOT NULL DEFAULT 0,
  max_score     INT          NOT NULL DEFAULT 100,
  max_attempts  INT          NOT NULL DEFAULT 10,
  is_active     BOOLEAN      NOT NULL DEFAULT TRUE,
  sort_order    INT          NOT NULL DEFAULT 0,
  created_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at    DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_qualification_test_slug (slug),
  KEY idx_qualification_test_active (is_active)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- One row per worker per test: attempts, best score, completed at (→ profile)
CREATE TABLE IF NOT EXISTS worker_test_results (
  id             BIGINT       NOT NULL AUTO_INCREMENT,
  user_id        BIGINT       NOT NULL,
  test_id        BIGINT       NOT NULL,
  attempts_count INT          NOT NULL DEFAULT 0,
  best_score     INT          NULL,
  status         VARCHAR(20)  NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, COMPLETED',
  completed_at   DATETIME(6)  NULL,
  created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
  PRIMARY KEY (id),
  UNIQUE KEY uq_worker_test (user_id, test_id),
  KEY idx_worker_test_user (user_id),
  KEY idx_worker_test_completed (status),
  CONSTRAINT fk_worker_test_user FOREIGN KEY (user_id) REFERENCES users(id),
  CONSTRAINT fk_worker_test_test FOREIGN KEY (test_id) REFERENCES qualification_tests(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- Seed a few tests (admin can add more)
INSERT IGNORE INTO qualification_tests (name, slug, test_type, min_score, max_score, max_attempts, is_active, sort_order) VALUES
  ('Typing Test', 'typing', 'TYPING', 30, 120, 10, TRUE, 1),
  ('Internet Speed', 'internet-speed', 'INTERNET_SPEED', 5, 100, 10, TRUE, 2),
  ('2 Minute Verbal', 'verbal', 'VERBAL', 1, 10, 10, TRUE, 3),
  ('Intermediate English (B1)', 'en-b1', 'ENGLISH_B1', 60, 100, 10, TRUE, 10),
  ('Upper-Intermediate English (B2)', 'en-b2', 'ENGLISH_B2', 60, 100, 10, TRUE, 11),
  ('Advanced English (C1)', 'en-c1', 'ENGLISH_C1', 60, 100, 10, TRUE, 12);

-- =============================================================================
-- End of v17-qualification-tests.sql
-- =============================================================================