# Backend — Kuongeza na Kutekeleza (Kama Guru / Upwork / OFM Jobs)

Hii ni orodha ya **vitu vya kuongeza na kutekeleza** ili backend iweze kufanya kazi kama Guru, Upwork, na OFM Jobs.

---

## ✅ Tayari ipo (summary)

| Moduli | Kile kinachofanya |
|--------|-------------------|
| **Auth** | Register (CLIENT/FREELANCER only), Login, JWT + role claim |
| **Users** | GET /users/me, PATCH /users/me (profile update), GET /users/{id}, reviews, rating |
| **Jobs** | Browse (search, category, budget), GET by id, POST (client), PUT/DELETE, my-jobs |
| **Proposals** | POST /jobs/{jobId}, my-proposals, applicants by job, PATCH status |
| **Contracts (Escrow)** | Hire, submit work, approve, dispute, my-contracts, milestones approve |
| **Wallet** | Balance, deposit/simulate, withdraw, transactions |
| **Chat** | Send/list messages, mark read, unread count |
| **Payments** | POST /payments/callback (M-Pesa) |
| **Notifications** | Service ina create + push WebSocket; getUserNotifications, countUnread, markAllAsRead |

---

## 1. Kuunganisha na Frontend (ili iwork) ✅

| Kitu | Kitendo |
|------|--------|
| **GET /api/auth/me** | Alias ya GET /api/users/me (frontend inatumia /auth/me). |
| **GET /api/jobs/categories** | Orodha ya categories (General, Development, …) kwa filters. |
| **PaginatedResponse** | DTO ya `{ data, total, page, limit }` kwa jobs list ikiwa frontend inahitaji. |

---

## 2. Categories & Tags (kama OFM Jobs) ✅

| Kitu | Kitendo |
|------|--------|
| **Job categories** | GET /api/jobs/categories (JobCategories.ALL). |
| **Suggested tags** | (Optional) GET /api/jobs/tags au validation ya tags. |

---

## 3. Notifications REST (kama Upwork/Guru) ✅

| Kitu | Kitendo |
|------|--------|
| **GET /api/notifications** | List ya notifications (paginated) kwa user. |
| **PATCH /api/notifications/{id}/read** | Mark one as read. |
| **GET /api/notifications/unread-count** | Idadi ya unread. |
| **PATCH /api/notifications/read-all** | Mark all as read. |

---

## 4. Admin (kama Guru/Upwork) ✅

| Kitu | Kitendo |
|------|--------|
| **GET /api/admin/users** | Orodha ya users (admin only). |
| **POST /api/admin/users/{id}/verify** | Verify user (set isVerified + verificationStatus). |
| **GET /api/admin/stats** | Platform stats (users, jobs, contracts, revenue). |
| **GET /api/admin/disputes** | List disputed contracts. |
| **PATCH /api/admin/disputes/{id}/resolve** | Resolve: body `{ "releaseTo": "CLIENT" \| "FREELANCER" }`. |

---

## 5. M-Pesa halisi (Tanzania) ✅

| Kitu | Kitendo |
|------|--------|
| **Config** | app.mpesa.* (enabled, base-url, consumer-key, consumer-secret, shortcode, passkey, callback-url). |
| **STK push** | POST /api/wallet/deposit/initiate (amount, phoneNumber) → Daraja STK push when enabled. |
| **Callback** | POST /api/payments/callback: parses Daraja Body.stkCallback (ResultCode 0 = success), credits wallet. |
| **Tigo Pesa / Airtel** | Phase 2. |

---

## 6. Zingine (optional) — Saved jobs & Reviews ✅

| Kitu | Kitendo |
|------|--------|
| **Saved jobs (bookmarks)** | GET /api/jobs/saved, POST /api/jobs/{id}/save, DELETE /api/jobs/{id}/save. |
| **Reviews/Ratings** | POST /api/contracts/{id}/reviews (rating 1–5 + comment). GET /api/users/{id}/reviews, GET /api/users/{id}/rating. |
| **File upload** | POST /api/upload (file, type=profile\|job\|proposal\|general). GET /uploads/** served from app.upload.dir. |

---

## Muhtasari wa kipaumbele

1. **Auth/me + categories + pagination** — frontend iweze ku-connect.
2. **Notifications REST** — list, mark read.
3. **Admin** — verify user, stats, disputes.
4. **M-Pesa** — integration halisi.
5. **Reviews / bookmarks / files** — baadaye.
