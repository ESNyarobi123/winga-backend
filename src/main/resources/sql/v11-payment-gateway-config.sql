-- =============================================================================
-- Winga — Payment gateway config (admin: API keys, M-Pesa, PayPal, etc.)
-- Run after v4-update.sql. Safe to run (CREATE TABLE IF NOT EXISTS).
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v11-payment-gateway-config.sql
-- =============================================================================

USE winga_db;

CREATE TABLE IF NOT EXISTS payment_gateway_config (
  id            BIGINT       NOT NULL AUTO_INCREMENT,
  gateway_slug  VARCHAR(50)  NOT NULL,
  display_name  VARCHAR(100) NOT NULL,
  config_json   TEXT         DEFAULT NULL,
  is_active     TINYINT(1)   NOT NULL DEFAULT 1,
  created_at    DATETIME(6)  NOT NULL,
  updated_at    DATETIME(6)  DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY idx_payment_gateway_slug (gateway_slug)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- =============================================================================
-- End of v11-payment-gateway-config.sql
-- =============================================================================
