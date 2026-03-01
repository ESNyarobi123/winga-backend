-- =============================================================================
-- Admin login for dashboard — add or update admin user
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v2-admin-login.sql
-- =============================================================================

USE winga_db;

-- -----------------------------------------------------------------------------
-- Option A: Insert new admin user (skip if email already exists)
-- Login: admin@winga.co.tz / Admin@1234
-- Password hash: BCrypt(12) for "Admin@1234"
-- -----------------------------------------------------------------------------
INSERT IGNORE INTO users (
  email,
  password_hash,
  full_name,
  phone_number,
  role,
  is_verified,
  verification_status,
  profile_completeness,
  is_active,
  created_at
) VALUES (
  'admin@winga.co.tz',
  '$2a$12$2Q/nszQgmQWgVyS6B8Tu6.FhwJXYLBP5KEzRjuU/IelocfPZC7Cka',
  'Winga Admin',
  '+255712000001',
  'ADMIN',
  1,
  'VERIFIED',
  0,
  1,
  NOW()
);

-- Wallet for admin (ignore if wallet already exists for this user)
INSERT IGNORE INTO wallets (user_id, balance, currency, total_earned, total_spent)
SELECT id, 0.00, 'TZS', 0.00, 0.00 FROM users WHERE email = 'admin@winga.co.tz' LIMIT 1;

-- -----------------------------------------------------------------------------
-- Option B: Update existing user to admin + set password (e.g. reset admin password)
-- Uncomment and set email + BCrypt hash. Generate hash:
--   mvn exec:java -Dexec.mainClass="com.winga.util.BCryptHashGenerator" -Dexec.args="Admin@1234"
-- -----------------------------------------------------------------------------
-- UPDATE users
-- SET role = 'ADMIN',
--     password_hash = '$2a$12$2Q/nszQgmQWgVyS6B8Tu6.FhwJXYLBP5KEzRjuU/IelocfPZC7Cka',
--     is_verified = 1,
--     verification_status = 'VERIFIED',
--     updated_at = NOW()
-- WHERE email = 'admin@winga.co.tz';

-- -----------------------------------------------------------------------------
-- Option C: Add a second admin (e.g. Super Admin)
-- Generate hash first, then run:
-- INSERT INTO users (email, password_hash, full_name, phone_number, role, is_verified, verification_status, profile_completeness, is_active, created_at)
-- VALUES ('superadmin@winga.co.tz', '$2a$12$YOUR_HASH', 'Super Admin', '+255712000099', 'SUPER_ADMIN', 1, 'VERIFIED', 0, 1, NOW());
-- INSERT INTO wallets (user_id, balance, currency) SELECT id, 0.00, 'TZS' FROM users WHERE email = 'superadmin@winga.co.tz' LIMIT 1;
-- -----------------------------------------------------------------------------
