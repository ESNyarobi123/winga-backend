-- =============================================================================
-- Winga — Backfill profile_completeness for existing FREELANCERs
-- Required fields: full_name, country, headline, languages, payment_preferences,
--                  work_type, timezone. Each = 1/7, total 0–100.
-- Run after v13-profile-verified.sql.
-- =============================================================================

USE winga_db;

-- Backfill: set profile_completeness for all FREELANCERs based on 7 required fields
UPDATE users
SET profile_completeness = LEAST(100, FLOOR(
    (IF(TRIM(COALESCE(full_name, '')) != '', 1, 0) +
     IF(TRIM(COALESCE(country, '')) != '', 1, 0) +
     IF(TRIM(COALESCE(headline, '')) != '', 1, 0) +
     IF(TRIM(COALESCE(languages, '')) != '', 1, 0) +
     IF(TRIM(COALESCE(payment_preferences, '')) != '', 1, 0) +
     IF(TRIM(COALESCE(work_type, '')) != '', 1, 0) +
     IF(TRIM(COALESCE(timezone, '')) != '', 1, 0)
    ) * 100 / 7
))
WHERE role = 'FREELANCER';

-- =============================================================================
-- End of v14-backfill-profile-completeness.sql
-- =============================================================================
