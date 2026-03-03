-- =============================================================================
-- Winga — Subscription plans (admin-managed packages for freelancers)
-- Run after v14. Creates subscription_plans table.
-- =============================================================================

USE winga_db;

CREATE TABLE IF NOT EXISTS subscription_plans (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description VARCHAR(500) DEFAULT NULL,
    price DECIMAL(12, 2) NOT NULL DEFAULT 0,
    currency VARCHAR(10) NOT NULL DEFAULT 'TZS',
    duration_days INT NOT NULL DEFAULT 30,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    sort_order INT NOT NULL DEFAULT 0,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE KEY uk_subscription_plan_slug (slug)
);

-- Seed default plan
INSERT IGNORE INTO subscription_plans (name, slug, description, price, currency, duration_days, is_active, sort_order)
VALUES ('Monthly Provider', 'monthly_provider', 'Submit proposals and get hired. Valid for 30 days.', 10000.00, 'TZS', 30, TRUE, 0);

-- =============================================================================
-- End of v15-subscription-plans.sql
-- =============================================================================
