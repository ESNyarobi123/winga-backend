-- =============================================================================
-- Winga — Seed demo freelancers (for Find Workers page)
-- Run after schema + v2. Safe to run multiple times (INSERT IGNORE).
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v7-seed-freelancers.sql
-- Login: david@freelancer.tz / Admin@1234  and  fatuma@freelancer.tz / Admin@1234 (demo)
-- =============================================================================

USE winga_db;

-- BCrypt(12) hash for "Admin@1234" (same as admin login for demo)
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
) VALUES
  ('david@freelancer.tz', '$2a$12$2Q/nszQgmQWgVyS6B8Tu6.FhwJXYLBP5KEzRjuU/IelocfPZC7Cka', 'David Ochieng', '+255712000004', 'FREELANCER', 1, 'VERIFIED', 0, 1, NOW()),
  ('fatuma@freelancer.tz', '$2a$12$2Q/nszQgmQWgVyS6B8Tu6.FhwJXYLBP5KEzRjuU/IelocfPZC7Cka', 'Fatuma Said', '+255712000005', 'FREELANCER', 1, 'VERIFIED', 0, 1, NOW());

INSERT IGNORE INTO wallets (user_id, balance, currency, total_earned, total_spent)
SELECT id, 0.00, 'TZS', 0.00, 0.00 FROM users WHERE email = 'david@freelancer.tz' LIMIT 1;

INSERT IGNORE INTO wallets (user_id, balance, currency, total_earned, total_spent)
SELECT id, 0.00, 'TZS', 0.00, 0.00 FROM users WHERE email = 'fatuma@freelancer.tz' LIMIT 1;

-- =============================================================================
-- End of v7-seed-freelancers.sql
-- =============================================================================
