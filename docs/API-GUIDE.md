# Winga API — Quick guide for frontend

Base URL: `http://localhost:8080` (or your backend URL).

## Auth

- **POST** `/api/auth/login` — Body: `{ "email", "password" }`. Returns `{ "data": { "accessToken", "refreshToken", "user" } }`.
- **POST** `/api/auth/forgot-password` — Body: `{ "email" }`.
- **POST** `/api/auth/reset-password` — Body: `{ "email", "otp", "newPassword" }`.
- **Rate limit:** login 15/min, forgot-password 5/min per IP. 429 when exceeded.

## Profile (worker)

- **GET** `/api/users/me` — Returns full profile. For FREELANCER includes:
  - `profileCompleteness` (0–100)
  - `isProfileComplete` (boolean)
  - `profileVerified`, `profileVerifiedAt` (admin-set badge)
  - `profileImageUrl`, `cvUrl`, `headline`, `country`, `languages`, `workType`, `timezone`, `paymentPreferences`, etc.
- **GET** `/api/users/me/profile-checklist` — Returns `{ "complete", "profileCompleteness", "missingFields" }` for onboarding UI (e.g. "Jaza: country, timezone").
- **PATCH** `/api/users/me` — Update profile. For FREELANCER, all of fullName, country, headline, languages, paymentPreferences, workType, timezone are **required**; otherwise 400: `"Profile incomplete. Required fields: ..."`.

## Upload

- **POST** `/api/upload` — `multipart/form-data`: `file`, `type` (e.g. `profile`, `cv`). Returns `{ "data": { "url": "/uploads/profile/xxx.jpg" } }`. Use this URL in PATCH profile for `profileImageUrl` or `cvUrl`. Rate limit: 30/min per IP.

## Workers (public)

- **GET** `/api/workers` — Query params: `keyword` (searches fullName, headline, skills, …), `employmentType`, `language`, `skill`, `categoryId`, `page`, `size`, `sort`.

## Admin

- **GET** `/api/admin/export/workers?incompleteOnly=true&withCvOnly=true` — CSV download.
- **POST** `/api/admin/export/workers` — Body: `{ "userIds": [1,2,3] }` — CSV of selected workers.
- **PUT** `/api/admin/users/{id}/verify-profile?verified=true` — Set profile verified badge.
- **POST** `/api/admin/users/bulk-verify-profile` — Body: `{ "userIds": [1,2,3], "verified": true }`.

## Error response shape

- Success: `{ "success": true, "data": ..., "message": null }`.
- Error: `{ "success": false, "message": "Profile incomplete. Required fields: ...", "data": null }`.
- 429: `{ "success": false, "message": "Too many requests. Please try again later." }`.
