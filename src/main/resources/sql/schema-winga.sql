-- =============================================================================
-- Winga — MySQL Schema (XAMPP / MySQL 5.7+)
--
-- Import:
--   mysql -u root -p winga_db < src/main/resources/sql/schema-winga.sql
-- Or phpMyAdmin: Import → Choose this file (create winga_db first if needed).
--
-- Warning: This script DROPS existing tables and recreates them (empty DB).
-- Backup your data first if you need to keep it.
-- =============================================================================

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- -----------------------------------------------------------------------------
-- Database
-- -----------------------------------------------------------------------------
CREATE DATABASE IF NOT EXISTS winga_db
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE winga_db;

-- Drop in reverse dependency order (child tables first)
DROP TABLE IF EXISTS wallet_transactions;
DROP TABLE IF EXISTS notifications;
DROP TABLE IF EXISTS chat_messages;
DROP TABLE IF EXISTS reviews;
DROP TABLE IF EXISTS contract_add_ons;
DROP TABLE IF EXISTS milestones;
DROP TABLE IF EXISTS contracts;
DROP TABLE IF EXISTS proposals;
DROP TABLE IF EXISTS saved_jobs;
DROP TABLE IF EXISTS jobs;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS platform_revenue;
DROP TABLE IF EXISTS users;

-- -----------------------------------------------------------------------------
-- 1. USERS (Auth & Identity)
-- -----------------------------------------------------------------------------

CREATE TABLE users (
  id                BIGINT        NOT NULL AUTO_INCREMENT,
  email             VARCHAR(100)  NOT NULL,
  password_hash     VARCHAR(255)  NOT NULL,
  full_name         VARCHAR(100)  NOT NULL,
  phone_number      VARCHAR(20)   DEFAULT NULL,
  role              VARCHAR(20)   NOT NULL,
  profile_image_url VARCHAR(500)  DEFAULT NULL,
  bio               VARCHAR(1000) DEFAULT NULL,
  skills            VARCHAR(100)  DEFAULT NULL,
  industry          VARCHAR(100)  DEFAULT NULL,
  company_name      VARCHAR(200)  DEFAULT NULL,
  is_verified       TINYINT(1)    NOT NULL DEFAULT 0,
  verification_status VARCHAR(20)  DEFAULT 'UNVERIFIED',
  profile_completeness INT        NOT NULL DEFAULT 0,
  is_active         TINYINT(1)    NOT NULL DEFAULT 1,
  created_at        DATETIME(6)   NOT NULL,
  updated_at        DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY idx_user_email (email),
  KEY idx_user_role (role),
  KEY idx_user_phone (phone_number),
  KEY idx_user_created (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 2. PROFILES (One-to-One with users)
-- -----------------------------------------------------------------------------
CREATE TABLE profiles (
  id            BIGINT         NOT NULL AUTO_INCREMENT,
  user_id       BIGINT         NOT NULL,
  headline      VARCHAR(200)   DEFAULT NULL,
  bio           TEXT          DEFAULT NULL,
  skills        TEXT          DEFAULT NULL,
  hourly_rate   DECIMAL(18,2)  DEFAULT NULL,
  portfolio_url VARCHAR(500)  DEFAULT NULL,
  created_at    DATETIME(6)   NOT NULL,
  updated_at    DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY idx_profile_user (user_id),
  CONSTRAINT fk_profile_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 3. WALLETS
-- -----------------------------------------------------------------------------

CREATE TABLE wallets (
  id              BIGINT         NOT NULL AUTO_INCREMENT,
  user_id         BIGINT         NOT NULL,
  balance         DECIMAL(18,2)  NOT NULL DEFAULT 0.00,
  currency        VARCHAR(10)    NOT NULL DEFAULT 'TZS',
  total_earned    DECIMAL(18,2)  DEFAULT 0.00,
  total_spent     DECIMAL(18,2)  DEFAULT 0.00,
  last_updated_at DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_wallet_user (user_id),
  CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4. JOBS (Job Board)
-- -----------------------------------------------------------------------------

CREATE TABLE jobs (
  id                BIGINT         NOT NULL AUTO_INCREMENT,
  client_id         BIGINT         NOT NULL,
  title             VARCHAR(200)   NOT NULL,
  description       TEXT           NOT NULL,
  budget            DECIMAL(18,2)  NOT NULL,
  budget_min        DECIMAL(18,2)  DEFAULT NULL,
  budget_max        DECIMAL(18,2)  DEFAULT NULL,
  type              VARCHAR(20)    DEFAULT NULL,
  deadline          DATE           DEFAULT NULL,
  status            VARCHAR(20)    NOT NULL DEFAULT 'OPEN',
  tags              VARCHAR(500)   DEFAULT NULL,
  category          VARCHAR(100)   DEFAULT NULL,
  experience_level  VARCHAR(50)    DEFAULT NULL,
  view_count        BIGINT         NOT NULL DEFAULT 0,
  created_at        DATETIME(6)   NOT NULL,
  updated_at        DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_job_client (client_id),
  KEY idx_job_status (status),
  KEY idx_job_created (created_at),
  KEY idx_job_category (category),
  CONSTRAINT fk_job_client FOREIGN KEY (client_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 4b. SAVED JOBS (Bookmarks)
-- -----------------------------------------------------------------------------
CREATE TABLE saved_jobs (
  id         BIGINT       NOT NULL AUTO_INCREMENT,
  user_id    BIGINT       NOT NULL,
  job_id     BIGINT       NOT NULL,
  created_at DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_saved_user_job (user_id, job_id),
  KEY idx_saved_user (user_id),
  KEY idx_saved_job (job_id),
  CONSTRAINT fk_saved_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_saved_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 5. PROPOSALS
-- -----------------------------------------------------------------------------
CREATE TABLE proposals (
  id                 BIGINT         NOT NULL AUTO_INCREMENT,
  job_id             BIGINT         NOT NULL,
  freelancer_id      BIGINT         NOT NULL,
  cover_letter       TEXT           NOT NULL,
  bid_amount         DECIMAL(18,2)  NOT NULL,
  estimated_duration VARCHAR(100)   DEFAULT NULL,
  status             VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  client_note        VARCHAR(1000)  DEFAULT NULL,
  revision_limit     INT            NOT NULL DEFAULT 3,
  created_at         DATETIME(6)   NOT NULL,
  updated_at         DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_proposal_job_freelancer (job_id, freelancer_id),
  KEY idx_proposal_job (job_id),
  KEY idx_proposal_freelancer (freelancer_id),
  KEY idx_proposal_status (status),
  CONSTRAINT fk_proposal_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
  CONSTRAINT fk_proposal_freelancer FOREIGN KEY (freelancer_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6. CONTRACTS (Escrow)
-- -----------------------------------------------------------------------------

CREATE TABLE contracts (
  id                     BIGINT         NOT NULL AUTO_INCREMENT,
  job_id                  BIGINT         NOT NULL,
  proposal_id             BIGINT         NOT NULL,
  client_id               BIGINT         NOT NULL,
  freelancer_id           BIGINT         NOT NULL,
  total_amount            DECIMAL(18,2)  NOT NULL,
  escrow_amount           DECIMAL(18,2)  NOT NULL DEFAULT 0.00,
  released_amount         DECIMAL(18,2)  NOT NULL DEFAULT 0.00,
  platform_fee_collected  DECIMAL(18,2)  NOT NULL DEFAULT 0.00,
  status                  VARCHAR(20)   NOT NULL DEFAULT 'ACTIVE',
  revision_limit          INT           NOT NULL DEFAULT 3,
  revisions_used          INT           NOT NULL DEFAULT 0,
  addon_escrow_amount     DECIMAL(18,2) NOT NULL DEFAULT 0.00,
  termination_reason      TEXT          DEFAULT NULL,
  completed_at            DATETIME(6)   DEFAULT NULL,
  created_at              DATETIME(6)   NOT NULL,
  updated_at              DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_contract_proposal (proposal_id),
  KEY idx_contract_client (client_id),
  KEY idx_contract_freelancer (freelancer_id),
  KEY idx_contract_status (status),
  CONSTRAINT fk_contract_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE CASCADE,
  CONSTRAINT fk_contract_proposal FOREIGN KEY (proposal_id) REFERENCES proposals (id) ON DELETE CASCADE,
  CONSTRAINT fk_contract_client FOREIGN KEY (client_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_contract_freelancer FOREIGN KEY (freelancer_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6a. CONTRACT ADD-ONS (Propose Extra Contract)
-- -----------------------------------------------------------------------------
CREATE TABLE contract_add_ons (
  id           BIGINT         NOT NULL AUTO_INCREMENT,
  contract_id  BIGINT         NOT NULL,
  title        VARCHAR(200)   NOT NULL,
  description  TEXT           DEFAULT NULL,
  amount       DECIMAL(18,2)  NOT NULL,
  status       VARCHAR(20)    NOT NULL DEFAULT 'PROPOSED',
  accepted_at  DATETIME(6)    DEFAULT NULL,
  completed_at DATETIME(6)    DEFAULT NULL,
  created_at   DATETIME(6)    NOT NULL,
  updated_at   DATETIME(6)    DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_addon_contract (contract_id),
  KEY idx_addon_status (status),
  CONSTRAINT fk_addon_contract FOREIGN KEY (contract_id) REFERENCES contracts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 6b. REVIEWS (after contract completed)
-- -----------------------------------------------------------------------------
CREATE TABLE reviews (
  id          BIGINT       NOT NULL AUTO_INCREMENT,
  contract_id  BIGINT       NOT NULL,
  reviewer_id  BIGINT       NOT NULL,
  reviewee_id  BIGINT       NOT NULL,
  rating       TINYINT      NOT NULL,
  comment      TEXT         DEFAULT NULL,
  created_at   DATETIME(6)  NOT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY uq_review_contract_reviewer (contract_id, reviewer_id),
  KEY idx_review_reviewee (reviewee_id),
  KEY idx_review_contract (contract_id),
  CONSTRAINT fk_review_contract FOREIGN KEY (contract_id) REFERENCES contracts (id) ON DELETE CASCADE,
  CONSTRAINT fk_review_reviewer FOREIGN KEY (reviewer_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_review_reviewee FOREIGN KEY (reviewee_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT chk_rating_range CHECK (rating >= 1 AND rating <= 5)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 7. MILESTONES
-- -----------------------------------------------------------------------------
CREATE TABLE milestones (
  id              BIGINT         NOT NULL AUTO_INCREMENT,
  contract_id     BIGINT         NOT NULL,
  title           VARCHAR(200)   NOT NULL,
  description     TEXT           DEFAULT NULL,
  amount          DECIMAL(18,2)  NOT NULL,
  due_date        DATE           DEFAULT NULL,
  order_index     INT            NOT NULL DEFAULT 0,
  status          VARCHAR(20)   NOT NULL DEFAULT 'PENDING',
  submission_note TEXT           DEFAULT NULL,
  review_note     TEXT           DEFAULT NULL,
  funded_at       DATETIME(6)   DEFAULT NULL,
  submitted_at    DATETIME(6)   DEFAULT NULL,
  approved_at     DATETIME(6)   DEFAULT NULL,
  created_at      DATETIME(6)   NOT NULL,
  updated_at      DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id),
  KEY idx_milestone_contract (contract_id),
  KEY idx_milestone_status (status),
  CONSTRAINT fk_milestone_contract FOREIGN KEY (contract_id) REFERENCES contracts (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 8. WALLET TRANSACTIONS
-- -----------------------------------------------------------------------------
CREATE TABLE wallet_transactions (
  id                      BIGINT         NOT NULL AUTO_INCREMENT,
  wallet_id               BIGINT         NOT NULL,
  transaction_type        VARCHAR(30)   NOT NULL,
  amount                  DECIMAL(18,2)  NOT NULL,
  balance_before          DECIMAL(18,2)  NOT NULL,
  balance_after           DECIMAL(18,2)  NOT NULL,
  description             VARCHAR(500)  DEFAULT NULL,
  reference_id            VARCHAR(100)   DEFAULT NULL,
  provider                VARCHAR(20)    DEFAULT NULL,
  external_transaction_id VARCHAR(50)    DEFAULT NULL,
  status                  VARCHAR(20)   DEFAULT 'COMPLETED',
  created_at              DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_tx_wallet (wallet_id),
  KEY idx_tx_type (transaction_type),
  KEY idx_tx_created (created_at),
  CONSTRAINT fk_tx_wallet FOREIGN KEY (wallet_id) REFERENCES wallets (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 9. CHAT MESSAGES
-- -----------------------------------------------------------------------------
CREATE TABLE chat_messages (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  job_id         BIGINT        DEFAULT NULL,
  contract_id    BIGINT        DEFAULT NULL,
  sender_id      BIGINT        NOT NULL,
  receiver_id    BIGINT        NOT NULL,
  content        TEXT         NOT NULL,
  attachment_url VARCHAR(500)  DEFAULT NULL,
  message_type   VARCHAR(50)   DEFAULT NULL,
  is_read        TINYINT(1)    NOT NULL DEFAULT 0,
  timestamp      DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_chat_job (job_id),
  KEY idx_chat_contract (contract_id),
  KEY idx_chat_sender (sender_id),
  KEY idx_chat_receiver (receiver_id),
  KEY idx_chat_timestamp (timestamp),
  CONSTRAINT fk_chat_job FOREIGN KEY (job_id) REFERENCES jobs (id) ON DELETE SET NULL,
  CONSTRAINT fk_chat_contract FOREIGN KEY (contract_id) REFERENCES contracts (id) ON DELETE SET NULL,
  CONSTRAINT fk_chat_sender FOREIGN KEY (sender_id) REFERENCES users (id) ON DELETE CASCADE,
  CONSTRAINT fk_chat_receiver FOREIGN KEY (receiver_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 10. NOTIFICATIONS
-- -----------------------------------------------------------------------------

CREATE TABLE notifications (
  id             BIGINT        NOT NULL AUTO_INCREMENT,
  user_id        BIGINT        NOT NULL,
  type           VARCHAR(50)   NOT NULL,
  title          VARCHAR(200)  NOT NULL,
  message        TEXT         NOT NULL,
  action_url     VARCHAR(200)  DEFAULT NULL,
  is_read        TINYINT(1)    NOT NULL DEFAULT 0,
  reference_id   VARCHAR(100)  DEFAULT NULL,
  reference_type VARCHAR(50)   DEFAULT NULL,
  created_at     DATETIME(6)   NOT NULL,
  PRIMARY KEY (id),
  KEY idx_notif_user (user_id),
  KEY idx_notif_is_read (is_read),
  KEY idx_notif_created (created_at),
  CONSTRAINT fk_notif_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- -----------------------------------------------------------------------------
-- 11. PLATFORM REVENUE
-- -----------------------------------------------------------------------------

CREATE TABLE platform_revenue (
  id                 BIGINT         NOT NULL AUTO_INCREMENT,
  period             VARCHAR(50)   NOT NULL,
  total_fees         DECIMAL(18,2)  NOT NULL DEFAULT 0.00,
  total_transactions BIGINT         NOT NULL DEFAULT 0,
  created_at         DATETIME(6)   NOT NULL,
  updated_at         DATETIME(6)   DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

SET FOREIGN_KEY_CHECKS = 1;

-- =============================================================================
-- End of schema
-- =============================================================================
