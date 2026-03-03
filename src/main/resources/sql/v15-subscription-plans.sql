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

-- Seed sample subscription plans (5 plans)
INSERT IGNORE INTO subscription_plans (name, slug, description, price, currency, duration_days, is_active, sort_order) VALUES
  ('Free Trial', 'free_trial', 'Try the platform: 1 job application. No featured profile.', 0.00, 'TZS', 7, TRUE, 1),
  ('Monthly Provider', 'monthly_provider', 'Submit proposals and get hired. Valid for 30 days.', 10000.00, 'TZS', 30, TRUE, 2),
  ('Quarterly Pro', 'quarterly_pro', '3 months access. Featured in find-workers. Priority support.', 25000.00, 'TZS', 90, TRUE, 3),
  ('Half Year', 'half_year', '6 months. Best value. Featured profile and badge.', 45000.00, 'TZS', 180, TRUE, 4),
  ('Yearly Premium', 'yearly_premium', 'Full year. Top placement, unlimited proposals, certification highlight.', 80000.00, 'TZS', 365, TRUE, 5);

-- =============================================================================
-- End of v15-subscription-plans.sql
-- =============================================================================
