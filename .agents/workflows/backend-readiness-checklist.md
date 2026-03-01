# Backend Readiness Checklist

Hii inaonyesha kuwa **kila kitu** unachohitaji kwa frontend (Auth, CLIENT, FREELANCER, ADMIN, Shared) **kipo kwenye backend** — API, role-based access, WebSocket, upload, notifications, error handling.

---

## 1. Auth na session

| Kitu | Backend | Maelezo |
|------|---------|---------|
| Register | ✅ | `POST /api/auth/register` — body: fullName, email, password, phoneNumber (optional), role (CLIENT \| FREELANCER). |
| Login | ✅ | `POST /api/auth/login` — email, password → `{ data: { accessToken, refreshToken, user } }`. |
| Kuhifadhi JWT | ✅ | Frontend inaweka token; kila request header `Authorization: Bearer <token>`. |
| GET /api/auth/me on load | ✅ | `GET /api/auth/me` — rudisha current user (id, fullName, email, role, isVerified, …). |
| Routing kwa role | ✅ | Backend: `@PreAuthorize("hasRole('CLIENT')")` n.k.; UserResponse ina `role` — frontend itumie kwa routes. |

---

## 2. CLIENT (mteja)

| Kitu | Backend | Endpoint / maelezo |
|------|---------|---------------------|
| Post job | ✅ | `POST /api/jobs` (title, description, budget, deadline, category, tags, experienceLevel). |
| Edit job | ✅ | `PUT /api/jobs/{id}` (job OPEN only). |
| My Jobs | ✅ | `GET /api/jobs/my-jobs` (paginated). |
| Applicants | ✅ | `GET /api/proposals/jobs/{jobId}/applicants`. |
| Shortlist / Reject | ✅ | `PATCH /api/proposals/{proposalId}/status?status=SHORTLISTED|REJECTED`. |
| Hire | ✅ | `POST /api/contracts/hire/{proposalId}` (optional body: releaseAmount). |
| Job chat (REST) | ✅ | `POST /api/chat/jobs/{jobId}/messages` (receiverId, content), `GET ...?otherUserId=`, `POST .../read`. |
| Job chat (WebSocket) | ✅ | Send `/app/chat/job/{jobId}` body `{ content, receiverId }`; subscribe `/topic/job.{jobId}` or `/user/queue/messages`. |
| My Contracts | ✅ | `GET /api/contracts/client/my-contracts`. |
| Approve work | ✅ | `POST /api/contracts/{id}/approve`. |
| Request changes | ✅ | `POST /api/contracts/{id}/request-changes` (revision limit checked). |
| Add milestone | ✅ | `POST /api/contracts/{id}/milestones` (title, amount, dueDate, orderIndex). |
| Approve milestone | ✅ | `POST /api/contracts/milestones/{milestoneId}/approve`. |
| Add-on accept | ✅ | `POST /api/contracts/{id}/add-ons/{addOnId}/accept` (client deposits add-on amount). |
| Add-on complete | ✅ | `POST /api/contracts/{id}/add-ons/{addOnId}/complete`. |
| End contract (client) | ✅ | `POST /api/contracts/{id}/terminate` — refunds escrow + addon escrow, job reopens. |
| Dispute | ✅ | `POST /api/contracts/{id}/dispute?reason=...`. |
| Contract chat (REST) | ✅ | `POST/GET /api/chat/contracts/{contractId}/messages`, `POST .../read`. |
| Contract chat (WebSocket) | ✅ | Send `/app/chat/contract/{contractId}`; subscribe `/topic/contract.{contractId}`. |
| Wallet balance | ✅ | `GET /api/wallet/balance`. |
| Deposit (M-Pesa) | ✅ | `POST /api/wallet/deposit/initiate` (amount, phoneNumber) → STK push. |
| Transaction history | ✅ | `GET /api/wallet/transactions`. |
| Reviews (leave) | ✅ | `POST /api/contracts/{id}/reviews` (rating, comment) — contract COMPLETED. |
| Profile | ✅ | `GET /api/users/me`, `PATCH /api/users/me`. |
| Notifications | ✅ | `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all`. |

---

## 3. FREELANCER

| Kitu | Backend | Endpoint / maelezo |
|------|---------|---------------------|
| Browse jobs | ✅ | `GET /api/jobs` (keyword, category, minBudget, maxBudget, page, size). |
| Job detail | ✅ | `GET /api/jobs/{id}`. |
| Save job | ✅ | `POST /api/jobs/{id}/save`, `GET /api/jobs/saved`, `DELETE /api/jobs/{id}/save`. |
| Submit proposal | ✅ | `POST /api/proposals/jobs/{jobId}` (coverLetter, bidAmount, estimatedDuration, revisionLimit). |
| My proposals | ✅ | `GET /api/proposals/my-proposals`. |
| Job chat | ✅ | Same REST + WebSocket as CLIENT (receiverId = client). |
| My contracts | ✅ | `GET /api/contracts/my-contracts`. |
| Submit work | ✅ | `POST /api/contracts/{id}/submit?note=...`. |
| Submit milestone | ✅ | `POST /api/contracts/{id}/milestones/{milestoneId}/submit?note=...`. |
| Propose add-on | ✅ | `POST /api/contracts/{id}/add-ons` (title, amount, description). |
| Dispute | ✅ | `POST /api/contracts/{id}/dispute?reason=...`. |
| Contract chat | ✅ | Same as CLIENT. |
| Wallet withdraw | ✅ | `POST /api/wallet/withdraw` (amount, mobileNumber, etc.). |
| Reviews (leave + my) | ✅ | `POST /api/contracts/{id}/reviews`; `GET /api/users/{id}/reviews`, `GET /api/users/{id}/rating`. |
| Profile & notifications | ✅ | Same as CLIENT. |

---

## 4. ADMIN

| Kitu | Backend | Endpoint / maelezo |
|------|---------|---------------------|
| Stats | ✅ | `GET /api/admin/stats` (totalUsers, totalClients, totalFreelancers, jobs, contracts, disputedContracts, totalPlatformRevenue). |
| List users | ✅ | `GET /api/admin/users` (paginated). |
| Verify user | ✅ | `POST /api/admin/users/{id}/verify?verify=true|false`. |
| List disputes | ✅ | `GET /api/admin/disputes`. |
| Dispute detail | ✅ | `GET /api/admin/disputes/{id}` (contract + job description + milestones + recent chat). |
| Resolve dispute | ✅ | `PATCH /api/admin/disputes/{id}/resolve` body `{ "releaseTo": "CLIENT" | "FREELANCER" }`. |

---

## 5. Shared (backend inasaidia)

| Kitu | Backend | Maelezo |
|------|---------|---------|
| Pagination | ✅ | Lists rudisha Spring `Page` (content, totalElements, totalPages, number, size) au `PaginatedResponse` (data, total, page, limit, totalPages). |
| File upload | ✅ | `POST /api/upload` multipart: `file`, `type` = profile \| job \| proposal \| general. Response `{ url }`. GET /uploads/** served. |
| WebSocket + JWT | ✅ | Connect to `/ws`; in STOMP CONNECT frame send header `Authorization: Bearer <accessToken>` or `token: <accessToken>`. Server validates JWT and sets Principal (email). Send/subscribe as in doc. |
| M-Pesa | ✅ | Initiate: `POST /api/wallet/deposit/initiate`. Callback: `POST /api/payments/callback`. Success/error in response/callback. |
| Notifications | ✅ | Unread count, list, mark read, read-all — see above. |
| Error handling | ✅ | 401 (Bad credentials), 403 (Forbidden), 404 (Not found), 422 (Validation), 402 (Insufficient funds). `ApiResponse`: success, message, data. |

---

## 6. Optional (baadaye)

| Kitu | Backend | Maelezo |
|------|---------|---------|
| Rate limiting (429) | ⏳ | Si implemented; frontend inaweza handle 429 ikiwa backend itaongeza. |
| Retry/offline | — | Frontend only. |
| Push notifications | ⏳ | Backend inaweza kuongeza FCM/APNs later. |

---

**Hitimisho:** Backend ina **kila API na flow** unayohitaji kwa Auth, CLIENT, FREELANCER, ADMIN, na shared (pagination, upload, WebSocket, M-Pesa, notifications, errors). Frontend inabaki kuimplement UI na ku-call endpoints hizi.
