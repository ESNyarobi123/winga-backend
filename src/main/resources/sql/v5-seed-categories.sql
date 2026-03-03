-- =============================================================================
-- Winga — Seed job categories (idempotent)
-- Run after v4-update.sql. Safe to run multiple times (INSERT IGNORE on slug).
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v5-seed-categories.sql
-- =============================================================================

USE winga_db;

-- Seed job categories: display order by sort_order. Slugs are unique; IGNORE skips duplicates.
INSERT IGNORE INTO job_categories (name, slug, sort_order, created_at) VALUES
  ('Programming & Tech', 'programming-tech', 1, NOW()),
  ('Writing and translation', 'writing-and-translation', 2, NOW()),
  ('Real Estate Support', 'real-estate-support', 3, NOW()),
  ('Legal and Financial', 'legal-and-financial', 4, NOW()),
  ('Pets and Animal Care', 'pets-and-animal-care', 5, NOW()),
  ('Photography', 'photography', 6, NOW()),
  ('Administrative services', 'administrative-services', 7, NOW()),
  ('Others services', 'others-services', 8, NOW()),
  ('Career Services', 'career-services', 9, NOW()),
  ('Education and Tutoring', 'education-and-tutoring', 10, NOW()),
  ('Transport Services', 'transport-services', 11, NOW()),
  ('Skilled handyman', 'skilled-handyman', 12, NOW()),
  ('Home services', 'home-services', 13, NOW()),
  ('Creative & Digital', 'creative-digital', 14, NOW()),
  ('Project supervision', 'project-supervision', 15, NOW()),
  ('Health & care', 'health-care', 16, NOW()),
  ('Care & Support', 'care-support', 17, NOW()),
  ('Delivery services', 'delivery-services', 18, NOW()),
  ('Personal assistance', 'personal-assistance', 19, NOW());

-- =============================================================================
-- End of v5-seed-categories.sql
-- =============================================================================
