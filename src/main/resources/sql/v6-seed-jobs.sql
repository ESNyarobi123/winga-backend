-- =============================================================================
-- Winga — Seed ~20 sample jobs (idempotent: demo client + jobs)
-- Run after v4-update.sql and v5-seed-categories.sql. Requires existing DB.
--
-- Run: mysql -u root -p winga_db < src/main/resources/sql/v6-seed-jobs.sql
-- =============================================================================

USE winga_db;

-- -----------------------------------------------------------------------------
-- 1. Demo client user (for posting jobs). Skip if email exists.
--    Login: demo-client@winga.co.tz / Admin@1234
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
  'demo-client@winga.co.tz',
  '$2a$12$2Q/nszQgmQWgVyS6B8Tu6.FhwJXYLBP5KEzRjuU/IelocfPZC7Cka',
  'Demo Client',
  '+255712000100',
  'CLIENT',
  1,
  'VERIFIED',
  0,
  1,
  NOW()
);

INSERT IGNORE INTO wallets (user_id, balance, currency, total_earned, total_spent)
SELECT id, 0.00, 'TZS', 0.00, 0.00 FROM users WHERE email = 'demo-client@winga.co.tz' LIMIT 1;

-- -----------------------------------------------------------------------------
-- 2. Seed 20 sample jobs (posted by demo client). Uses first user with this email.
-- -----------------------------------------------------------------------------
SET @cid = (SELECT id FROM users WHERE email = 'demo-client@winga.co.tz' LIMIT 1);

-- If no demo client (e.g. IGNORE skipped insert), use admin as fallback so script does not fail
SET @cid = IFNULL(@cid, (SELECT id FROM users WHERE role = 'ADMIN' LIMIT 1));

INSERT INTO jobs (
  client_id,
  title,
  description,
  budget,
  type,
  deadline,
  status,
  tags,
  category,
  experience_level,
  view_count,
  moderation_status,
  is_featured,
  is_boosted_telegram,
  created_at,
  updated_at
) VALUES
(@cid, 'Website development for small business', 'Need a simple responsive website for my shop. 5–7 pages, contact form, and mobile-friendly. Prefer someone with portfolio.', 850000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 21 DAY), 'OPEN', 'web,wordpress,responsive', 'Programming & Tech', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Translate Swahili document to English', 'Legal document about 15 pages. Need accurate translation and proofread. Native English preferred.', 250000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'OPEN', 'translation,swahili,english', 'Writing and translation', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Property listing data entry and photos', 'Real estate agency needs help entering listings and organizing property photos. 20–30 listings per week.', 150000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'OPEN', 'data entry,real estate', 'Real Estate Support', 'JUNIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Bookkeeping and VAT filing support', 'Monthly bookkeeping for small business. Prepare records for accountant and assist with VAT filing. 2–3 days per month.', 300000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'OPEN', 'bookkeeping,vat,accounts', 'Legal and Financial', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Dog walking and feeding (Dar es Salaam)', 'Need someone to walk and feed my dog 5 days a week, 1 hour per day. Area: Oyster Bay. Reliable and love animals.', 200000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'OPEN', 'pets,dog walking', 'Pets and Animal Care', 'JUNIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Event photography – wedding (1 day)', 'Full day wedding photography. Need edited high-res photos, at least 200 images. Date in 6 weeks.', 1200000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 42 DAY), 'OPEN', 'photography,wedding,events', 'Photography', 'MID', 0, 'APPROVED', 1, 0, NOW(), NOW()),
(@cid, 'Virtual assistant – emails and calendar', 'Manage email inbox and calendar for 2 executives. 4 hours per day, Mon–Fri. Good English and organization skills.', 400000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'OPEN', 'VA,admin,calendar', 'Administrative services', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Miscellaneous errands and shopping', 'Weekly errands: shopping, bank visits, picking up documents. Flexible hours. Must be in Dar.', 120000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'OPEN', 'errands,personal assistant', 'Others services', 'JUNIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'CV and cover letter review (5 documents)', 'Review and improve 5 CVs and cover letters for job applications. Professional tone and ATS-friendly format.', 75000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 10 DAY), 'OPEN', 'CV,career,writing', 'Career Services', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Math and physics tutoring – Form 3 student', 'Tutor for Form 3 student, 3 times per week, 1.5 hours per session. Syllabus coverage and exam prep.', 180000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 60 DAY), 'OPEN', 'tutoring,math,physics', 'Education and Tutoring', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Airport pickup and drop-off service', 'Reliable driver for airport transfers (Dar–JNIA). Need car with good condition. Multiple trips per month.', 50000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'OPEN', 'transport,driver,airport', 'Transport Services', 'JUNIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Plumbing repair – blocked sink and toilet', 'Residential plumbing: blocked sink in kitchen and toilet. Need same-day or next-day fix. Kinondoni area.', 150000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 3 DAY), 'OPEN', 'plumbing,handyman', 'Skilled handyman', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Deep cleaning – 3-bedroom house', 'One-time deep clean before moving in. Windows, floors, bathrooms, kitchen. Provide own basic supplies.', 200000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'OPEN', 'cleaning,home', 'Home services', 'JUNIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Logo and social media graphics (5 designs)', 'Need logo plus 5 social media graphics for new brand. Modern, simple style. Source files required.', 350000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 21 DAY), 'OPEN', 'design,logo,graphics', 'Creative & Digital', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Site supervision – small construction project', 'Supervise small extension build. 2–3 site visits per week, report to owner. Basic construction knowledge required.', 450000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 90 DAY), 'OPEN', 'supervision,construction', 'Project supervision', 'SENIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Home care visits for elderly parent', 'Visit elderly parent 3 times per week: company, light meals, medication reminder. Kind and patient. Masaki area.', 250000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 30 DAY), 'OPEN', 'elderly care,home care', 'Health & care', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Childminding – 2 afternoons per week', 'Look after 2 children (4 and 7) after school, 2–3 hours per afternoon. Help with homework and light snack. Oyster Bay.', 100000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 14 DAY), 'OPEN', 'childcare,babysitting', 'Care & Support', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Same-day delivery – documents and parcels', 'Need reliable rider/driver for same-day delivery in Dar. 5–10 deliveries per week. Motorcycle or car ok.', 80000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 7 DAY), 'OPEN', 'delivery,courier', 'Delivery services', 'JUNIOR', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Personal assistant – scheduling and calls', 'Handle scheduling, phone calls, and light admin for busy professional. 3 hours daily, flexible. Good English.', 280000.00, 'HOURLY', DATE_ADD(CURDATE(), INTERVAL 21 DAY), 'OPEN', 'PA,scheduling,admin', 'Personal assistance', 'MID', 0, 'APPROVED', 0, 0, NOW(), NOW()),
(@cid, 'Mobile app (Android) – simple inventory', 'Simple Android app for shop inventory: add/edit items, basic reports. No backend; local storage first.', 1200000.00, 'FIXED_PRICE', DATE_ADD(CURDATE(), INTERVAL 45 DAY), 'OPEN', 'android,app,inventory', 'Programming & Tech', 'SENIOR', 0, 'APPROVED', 1, 0, NOW(), NOW());

-- =============================================================================
-- End of v6-seed-jobs.sql
-- =============================================================================
