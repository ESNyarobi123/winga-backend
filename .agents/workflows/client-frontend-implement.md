# Client (Frontend) — Vitu ya Kuongeza / Ku-implement

Hii inaorodhesha kile **frontend** (web/mobile) inahitaji ku-implement ili kufanya kazi na backend ya Winga (kama OFM/Upwork/Guru).

---

## 1. Auth na Session

| Kitu | Maelezo |
|------|---------|
| **Register** | `POST /api/auth/register` — body: email, password, fullName, role (CLIENT \| FREELANCER), phoneNumber optional. |
| **Login** | `POST /api/auth/login` — email, password → response ina `token` na user. |
| **Store JWT** | Weka token (localStorage/sessionStorage au httpOnly cookie); tumia header `Authorization: Bearer <token>` kwa kila request. |
| **Current user** | On app load / refresh: `GET /api/auth/me` → weka user + role kwenye state. |
| **Logout** | Futa token na clear user state. |
| **Role-based routing** | CLIENT → routes za mteja (post job, my jobs, hire, contracts). FREELANCER → browse, proposals, my contracts. ADMIN → /admin/*. |
| **Public routes** | Login, Register, Browse jobs (GET /api/jobs), View job (GET /api/jobs/{id}), Categories (GET /api/jobs/categories). |

---

## 2. CLIENT (Mteja) — Screens na API

- **Dashboard / My Jobs** — `GET /api/jobs/my-jobs` (paginated). Links: view job, view applicants.
- **Post Job** — form (title, description, categoryId, budget, deadline, tags) → `POST /api/jobs`. Optional: `POST /api/upload?type=job` kwa attachments.
- **Edit Job** — `GET /api/jobs/{id}`, form → `PUT /api/jobs/{id}` (job OPEN only).
- **Job detail (owner)** — View applicants: `GET /api/proposals/jobs/{jobId}/applicants`. Shortlist/Reject: `PATCH /api/proposals/{proposalId}/status?status=SHORTLISTED|REJECTED`. Hire: `POST /api/contracts/hire/{proposalId}` (balance must be ≥ escrow).
- **Job chat (with applicant)** — List: `GET /api/chat/jobs/{jobId}/messages?otherUserId=<freelancerId>`. Send: `POST /api/chat/jobs/{jobId}/messages` (receiverId, content). Mark read: `POST /api/chat/jobs/{jobId}/read`. **WebSocket:** subscribe `/topic/job.{jobId}`, send `/app/chat/job/{jobId}` { content, receiverId }.
- **My Contracts (client)** — `GET /api/contracts/client/my-contracts`. Contract detail: `GET /api/contracts/{id}` (milestones, add-ons, status).
- **Contract actions (client)** — Approve: `POST /api/contracts/{id}/approve`. Request changes: `POST /api/contracts/{id}/request-changes` (revision limit check). Add milestone: `POST /api/contracts/{id}/milestones`. Approve milestone: `POST /api/contracts/milestones/{milestoneId}/approve`. Add-on: Accept → `POST /api/contracts/{id}/add-ons/{addOnId}/accept`, Complete → `POST .../add-ons/{addOnId}/complete`. Dispute: `POST /api/contracts/{id}/dispute?reason=...`.
- **Contract chat** — `GET/POST /api/chat/contracts/{contractId}/messages`, `POST .../read`. WebSocket: `/app/chat/contract/{contractId}`, subscribe `/topic/contract.{contractId}`.
- **Wallet** — Balance: `GET /api/wallet/balance`. Deposit: `POST /api/wallet/deposit/initiate` (amount, phone) → STK push; show “Enter PIN on phone” + poll balance or wait for success. History: `GET /api/wallet/transactions`.
- **Reviews** — Leave: `POST /api/contracts/{id}/reviews` (rating, comment) when contract COMPLETED.
- **Profile** — `GET /api/users/me`, `PATCH /api/users/me` (fullName, phoneNumber, bio, skills, profileImageUrl). Upload image: `POST /api/upload?type=profile`.
- **Notifications** — `GET /api/notifications`, `GET /api/notifications/unread-count`, `PATCH /api/notifications/{id}/read`, `PATCH /api/notifications/read-all`.

---

## 3. FREELANCER — Screens na API

- **Browse jobs** — `GET /api/jobs` (search, category, min/max budget, page). `GET /api/jobs/categories`. Save: `POST /api/jobs/{id}/save`, `DELETE /api/jobs/{id}/save`, `GET /api/jobs/saved`.
- **Job detail** — `GET /api/jobs/{id}`. Button “Apply” → proposal form.
- **Submit proposal** — `POST /api/proposals/jobs/{jobId}` (coverLetter, bidAmount, estimatedDuration, revisionLimit optional).
- **My proposals** — `GET /api/proposals/my-proposals`.
- **Job chat (with client)** — Same as CLIENT side but as sender: `receiverId` = client id; WebSocket same.
- **My contracts** — `GET /api/contracts/my-contracts`. Detail: `GET /api/contracts/{id}`.
- **Submit work** — `POST /api/contracts/{id}/submit?note=...`. Submit milestone: `POST /api/contracts/{id}/milestones/{milestoneId}/submit?note=...`.
- **Propose add-on** — `POST /api/contracts/{id}/add-ons` (title, amount). List: `GET /api/contracts/{id}/add-ons`. Reject/Complete (client); freelancer views status.
- **Dispute** — `POST /api/contracts/{id}/dispute?reason=...`.
- **Contract chat** — Same as CLIENT.
- **Wallet** — Balance, `POST /api/wallet/withdraw`, `GET /api/wallet/transactions`.
- **Leave review** — `POST /api/contracts/{id}/reviews`. My reviews: `GET /api/users/{id}/reviews`, `GET /api/users/{id}/rating`.
- **Profile & notifications** — Same idea as CLIENT; upload `type=proposal` for attachments.

---

## 4. ADMIN — Screens na API

- **Dashboard** — `GET /api/admin/stats` (totalUsers, jobs, contracts, disputedContracts, totalPlatformRevenue).
- **Users** — `GET /api/admin/users`. Verify: `POST /api/admin/users/{id}/verify?verify=true|false`.
- **Disputes** — `GET /api/admin/disputes`. Detail: `GET /api/admin/disputes/{id}` (contract + job + milestones + recent chat). Resolve: `PATCH /api/admin/disputes/{id}/resolve` body `{ "releaseTo": "CLIENT" | "FREELANCER" }`.

---

## 5. Shared (Frontend)

| Kitu | Maelezo |
|------|---------|
| **Base URL** | API base (e.g. `https://api.winga.co` au localhost:8080). CORS imeset kwenye backend. |
| **Pagination** | Backend inarudisha `PaginatedResponse` (content, totalElements, totalPages, size, number). Tumia page/size kwa lists. |
| **Errors** | Handle 401 (logout), 403 (forbidden), 4xx validation messages (backend inarudisha message). |
| **File upload** | `POST /api/upload` multipart: `file`, `type` = profile \| job \| proposal \| general. Response ina URL path; tumia kwa profile image, job attachment, proposal attachment. |
| **WebSocket** | Connect na JWT (e.g. query param `?token=` au subprotocol). Subscribe: `/topic/contract.{id}`, `/topic/job.{id}`, `/user/queue/messages`. Send: `/app/chat/contract/{contractId}`, `/app/chat/job/{jobId}` (job: body ina `receiverId`). |
| **M-Pesa deposit** | Call `POST /api/wallet/deposit/initiate` → user gets STK push; show “Check your phone, enter PIN”. On success, balance inaongezeka; optionally poll `GET /api/wallet/balance` or show success from callback (backend handles). |
| **Notifications** | Bell icon + unread count (`GET /api/notifications/unread-count`). List page: `GET /api/notifications`; mark read on open. |

---

## 6. Kwa kutumia uwezo wangu — Muhtasari

**Lazima ku-implement (core):**
- Auth flow (register, login, JWT, /me, role-based routes).
- CLIENT: post job, my jobs, applicants, hire, my contracts, approve/request-changes, milestones, add-on accept/complete, contract chat, wallet (balance, deposit, history), notifications, profile.
- FREELANCER: browse/save jobs, submit proposal, my proposals, job chat, my contracts, submit work/milestone, propose add-on, contract chat, wallet (withdraw), reviews, profile, notifications.
- ADMIN: stats, users (verify), disputes list + detail + resolve.

**Muhimu (UX):**
- WebSocket kwa chat (job + contract) ili real-time.
- File upload (profile, job, proposal).
- M-Pesa flow (initiate + UI ya “enter PIN” + success/error).
- Notifications (unread count + list + mark read).
- Pagination kwenye lists.
- Validation messages (backend error body).

**Optional (baadaye):**
- Rate limiting (backend tayari inaweza kuwa na; frontend ku-handle 429).
- Offline / retry logic.
- Push notifications (FCM/APNs) — backend inaweza kuongeza later.

Kila endpoint na flow zimeorodheshwa kwenye **roles-and-functions.md**; hii doc inafocus kwa **client side** tu: screens na vitu ya ku-implement.
