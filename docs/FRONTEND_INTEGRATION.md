# Frontend integration checklist

Use this with the API to align profile, workers, and admin features.

## Profile (worker)

| Feature | API | Notes |
|--------|-----|--------|
| **Profile completeness** | `GET /api/users/me` → `profileCompleteness`, `isProfileComplete` | Show progress bar or badge (e.g. 57% complete). |
| **Checklist / onboarding** | `GET /api/users/me/profile-checklist` → `complete`, `missingFields`, `profileCompleteness` | Show “Jaza: country, timezone, …” from `missingFields`. |
| **Validation errors** | `PATCH /api/users/me` → 400 body `{ "message": "Profile incomplete. Required fields: ..." }` | Display message under form. |
| **Profile verified badge** | `GET /api/users/me` or `GET /api/users/:id` → `profileVerified`, `profileVerifiedAt` | Show “Verified profile” when `profileVerified === true`. |

## Workers list

| Feature | API | Notes |
|--------|-----|--------|
| **Filters** | `GET /api/workers?keyword=&employmentType=&language=&skill=&categoryId=&profileVerified=&profileComplete=` | `profileVerified=true` and `profileComplete=true` for verified / complete-only. |
| **Search** | Same: `keyword` searches fullName, headline, skills, country, workType. | |

## Admin

| Feature | API | Notes |
|--------|-----|--------|
| **Bulk verify profile** | `POST /api/admin/users/bulk-verify-profile` body `{ "userIds": [1,2,3], "verified": true }` | Response `{ "data": { "updated": 3 } }`. |
| **Export selected workers** | `POST /api/admin/export/workers` body `{ "userIds": [1,2,3] }` | Returns CSV. |
| **Analytics** | `GET /api/admin/analytics?from=&to=` | Optional ISO date-time. Returns jobsPerCategory, proposalsPerJob, revenueInPeriod. |
| **Export contracts** | `GET /api/admin/export/contracts` | Returns CSV. |

## Auth

| Feature | API | Notes |
|--------|-----|--------|
| **Refresh token rotation** | `POST /api/auth/refresh` body `{ "refreshToken": "..." }` | Returns new accessToken + new refreshToken. Use the new refresh next time. |

## Notifications

| Feature | API | Notes |
|--------|-----|--------|
| **List (paginated)** | `GET /api/notifications?page=0&size=20` | Already paginated. |
| **Mark all read** | `PATCH /api/notifications/read-all` | Returns count. |
