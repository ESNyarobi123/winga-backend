# Winga Backend — What's Next

Flow na mfumo tumeziba kwenye [product-vision-and-flow.md](./product-vision-and-flow.md). Hii ni **orodha ya vipaumbele** vya kazi zinazofuata kwenye backend.

---

## ✅ Tayari ipo (summary)

| Kitu | Status |
|------|--------|
| Auth (register, login, JWT) | ✅ |
| User profile (/users/me, /users/{id}) | ✅ |
| Jobs (browse, create, update, delete, my-jobs, search/filter) | ✅ |
| Proposals (create, list by job, my-proposals, status) | ✅ |
| Contracts / Escrow (hire, submit, approve, dispute, milestones) | ✅ |
| Wallet (balance, deposit simulate, withdraw, transactions) | ✅ |
| Chat (messages, read, unread-count) | ✅ |
| Payment callback (M-Pesa webhook endpoint) | ✅ |
| Notifications (in-app + WebSocket push on hire, submit, release) | ✅ (hauko REST list) |
| Currency (TZS, USD enum) | ✅ |

---

## 1. Kuunganisha na Frontend (Priority 1)

Frontend (Winga ui) inatarajia response shape tofauti na baadhi ya paths. Ili frontend iweze ku-connect bila kukwepa:

| Kitu | Backend sasa | Frontend inatarajia | Kitendo |
|------|--------------|----------------------|---------|
| Auth response | `ApiResponse.success(message, AuthResponse)` → `data: { accessToken, refreshToken, user }` | `{ user, token }` | Frontend kuchukua `data.user` na `data.accessToken` kama `token`; au backend kuongeza optional wrapper. |
| Current user | `GET /api/users/me` | `GET /auth/me` | Kuongeza **alias**: `GET /api/auth/me` → redirect au duplicate handler inayoitumia UserController logic. |
| Jobs list | `Page<JobResponse>` (content, totalElements, number, size) | `{ data: [], total, page, limit }` | Kuongeza DTO `PaginatedResponse<T>` na kuitumia kwenye browse jobs (au frontend k adapt `Page`). |
| CORS | localhost:3000, 5173, winga.co.tz | - | Tayari ipo. |

**Action items:**

- [ ] Kuongeza `GET /api/auth/me` (proxy to same logic as `GET /api/users/me`).
- [ ] Kuchagua: ama backend inarudisha `PaginatedResponse` kwa jobs (na endpoints nyingine zenye page), ama frontend ina-adapt `Page` shape.
- [ ] Kuthibitisha frontend inatumia `response.data.data` (Axios + ApiResponse) na `accessToken` kama `token`.

---

## 2. Categories na Tags za Tanzania (Priority 2)

Kwenye [product-vision-and-flow.md](./product-vision-and-flow.md): tags/categories kwa soko la Tanzania (Graphics, Web Design, Uandishi, IT Support, Data Entry, Digital Marketing, n.k.).

| Kitu | Status | Kitendo |
|------|--------|---------|
| Job.category | String (free text) | ✅ ipo. |
| Job.tags | String (comma-separated) | ✅ ipo. |
| Orodha maalum ya categories | Haipo | Kuongeza enum au table ya **allowed categories**; optional: tags allowed. |
| Filter by category | ipo (`category` param) | ✅. |

**Action items:**

- [ ] Kuongeza `JobCategory` enum au config/orodha ya categories (kutumika kwenye validation na frontend dropdown).
- [ ] (Optional) Kuongeza suggested tags kwenye API (e.g. `GET /api/jobs/categories` na `GET /api/jobs/tags`) ili frontend iwe na filters sawa na OFM Jobs.

---

## 3. M-Pesa / Tigo Pesa / Airtel Money halisi (Priority 3)

Sasa: deposit ni **simulate**; callback ina basic parsing.

| Kitu | Status | Kitendo |
|------|--------|---------|
| Deposit simulate | ✅ | Waendelee kwa dev. |
| Payment callback body | Generic Map | Ku-standardize: Daraja (M-Pesa) callback format, validate signature. |
| Tigo Pesa / Airtel | Haijapo | Phase 2: gateway integration. |

**Action items:**

- [ ] Ku-integrate **M-Pesa Daraja API** (STK push, callback URL, validation).
- [ ] Kuthibitisha callback: parse payload, validate (e.g. checksum), update wallet, respond 200.
- [ ] (Later) Tigo Pesa / Airtel Money integration.

---

## 4. Notifications REST API (Priority 4)

Notifications zinaundwa na NotificationService (hire, submit, release, proposal received) na zinaweza kutumwa kwa WebSocket. Frontend inaweza kutaka **REST** pia (list, mark read).

**Action items:**

- [ ] Kuongeza `NotificationController`: e.g. `GET /api/notifications` (paginated), `PATCH /api/notifications/{id}/read`, `GET /api/notifications/unread-count`.
- [ ] Kuthibitisha WebSocket push ipo (tayari inaonekana kwenye NotificationService).

---

## 5. Verification (Verified Freelancer / Client) (Priority 5)

`User.isVerified` ipo. Product inataka "Verified Freelancer" / "Verified Client" (NIDA au simu).

**Action items:**

- [ ] Kuongeza endpoint (e.g. admin) au process: **verify user** (set `isVerified = true`).
- [ ] (Later) NIDA au phone verification flow (OTP, integration nje).

---

## 6. Dispute Resolution (Priority 6)

Contract ina **raise dispute**. Hakuna flow maalum ya kusuluhisha (admin hold/release escrow).

**Action items:**

- [ ] Kuongeza dispute status (e.g. OPEN, UNDER_REVIEW, RESOLVED).
- [ ] (Optional) Admin endpoints: list disputes, resolve (release to client vs freelancer), hold escrow until resolved.

---

## 7. Production Readiness (Obligatory kabla ya go-live)

| Kitu | Kitendo |
|------|---------|
| Database | `spring.jpa.hibernate.ddl-auto: validate` (au `none`); migrations (Flyway/Liquibase) kwa production. |
| Secrets | JWT, DB, mail: env variables / secret manager; si hardcoded. |
| Logging | Reduce SQL in prod; keep ERROR/WARN for security. |
| Rate limiting | (Optional) Limit login, deposit, callback. |
| Health | Actuator health tayari; ku-expose tu `/actuator/health` kwa load balancer. |

---

## Muhtasari wa kipaumbele

1. **Kwanza:** Alignment na frontend (auth/me, response shapes, pagination).
2. **Pili:** Categories/tags za Tanzania (enum au API ya categories/tags).
3. **Tatu:** M-Pesa integration halisi (Daraja).
4. **Nne:** Notifications REST (list, mark read).
5. **Tano:** Verification flow (admin verify / future NIDA).
6. **Sita:** Dispute resolution (status, admin resolve).
7. **Kabla ya production:** ddl-auto, secrets, logging, optional rate limit.

Kuanzia na **Priority 1** utafanya frontend ku-connect na backend bila kukwepa; kisha unaweza kufuata 2 → 3 → … kwa kipengele.
