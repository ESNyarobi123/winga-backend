# 🇹🇿 Winga — Freelance Marketplace Backend

> Tanzania's premier freelance marketplace — built on **Spring Boot 3.2**, featuring a secure **Escrow Engine**, **Mobile Money** integration, and **Real-time WebSocket** chat.

---

## 📂 Related project — Frontend (UI)

| Project   | Path        | Stack    |
|-----------|-------------|----------|
| **Backend**  | `winga-backend/` (this repo) | Spring Boot, Java 21 |
| **Frontend** | **`Winga ui/`**              | Next.js              |

Full path ya UI (mfano): `/Users/eunice/WORKS/Winga ui`.  
Detail zaidi: [docs/PROJECT-PATHS.md](docs/PROJECT-PATHS.md).

---

## 🏗️ Architecture Overview

```
winga-backend/
├── src/main/java/com/winga/
│   ├── WingaApplication.java          # Entry point
│   ├── config/                        # Security, WebSocket, OpenAPI, Async
│   ├── controller/                    # REST + WebSocket controllers
│   ├── domain/
│   │   ├── entity/                    # JPA entities
│   │   └── enums/                     # All enums
│   ├── dto/
│   │   ├── request/                   # Validated request records
│   │   └── response/                  # Response records (incl. ApiResponse<T>)
│   ├── exception/                     # Custom exceptions + GlobalExceptionHandler
│   ├── repository/                    # Spring Data JPA repositories
│   ├── security/                      # JWT service + filter
│   └── service/                       # Business logic
└── src/main/resources/
    └── application.yml
```

---

## ⚙️ Tech Stack

| Component        | Technology                             |
|-----------------|----------------------------------------|
| Language        | Java 21 (Records, Virtual Threads)     |
| Framework       | Spring Boot 3.2                        |
| Database        | MySQL 8.0                              |
| ORM             | Spring Data JPA (Hibernate)            |
| Authentication  | Spring Security 6 + JWT (JJWT 0.12)   |
| Real-time       | Spring WebSocket — STOMP + SockJS     |
| API Docs        | SpringDoc OpenAPI 2 (Swagger UI)       |
| Utilities       | Lombok, MapStruct                      |

---

## 🔐 Core Business Algorithms

### Algorithm 1 — Hire & Escrow Lock
```
POST /api/contracts/hire/{proposalId}
```
1. Validates client wallet balance ≥ bid amount
2. Deducts funds with **PESSIMISTIC_WRITE** lock (concurrent safe)
3. Creates `Contract` with `escrowAmount = bidAmount`
4. Sets `Job → IN_PROGRESS`, `Proposal → HIRED`
5. Notifies freelancer via WebSocket

### Algorithm 2 — Work Approval & Payout
```
POST /api/contracts/{id}/approve
```
1. `platformFee = escrowAmount × 10%`
2. `freelancerPay = escrowAmount - platformFee`
3. Credits freelancer wallet
4. Sets `Contract → COMPLETED`, `escrowAmount = 0`
5. Notifies freelancer of payment

### Algorithm 3 — Mobile Money Simulation
```
POST /api/wallet/deposit/simulate
```
Simulates M-Pesa / Tigo Pesa / Airtel Money callbacks instantly.

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** (recommended for build; Lombok has known issues with JDK 24+)
- Maven 3.9+
- MySQL 8.0+

If you see a compile error like `TypeTag :: UNKNOWN` (Lombok + JDK 24/25), use JDK 21 for building, e.g.:
`export JAVA_HOME=$(/usr/libexec/java_home -v 21)` then `./mvnw clean compile`.

### Setup

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE winga_db;"

# 2. Run migrations (in order; see Database migrations below)
mysql -u root -p winga_db < src/main/resources/sql/schema-winga.sql
# Then v1-admin-panel.sql, v2-admin-login.sql, v3-payment-options.sql, v4-update.sql
# Optional seeds: v5-seed-categories.sql, v6-seed-jobs.sql, v7-seed-freelancers.sql, etc.
# Worker profile: v12-worker-profile-fields.sql, v13-profile-verified.sql, v14-backfill-profile-completeness.sql

# 3. Configure credentials (or use environment variables)
# Edit src/main/resources/application.yml or set:
#   DB_USERNAME=root  DB_PASSWORD=yourpassword
#   JWT_SECRET=your-secret  (production)
#   UPLOAD_DIR=./uploads   UPLOAD_BASE_URL= (optional, for full URLs)

# 4. Run the application
./mvnw spring-boot:run

# 5. Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

### Database migrations (run order)

| Script | Purpose |
|--------|---------|
| `schema-winga.sql` | Base schema (users, jobs, contracts, …) |
| `v1-admin-panel.sql` … `v4-update.sql` | Admin, jobs, categories |
| `v5`–`v11` | Seeds, filter options, payment gateway, jobs columns |
| `v12-worker-profile-fields.sql` | Worker: headline, type_speed, has_webcam, skills_learned |
| `v13-profile-verified.sql` | Admin profile verification (profile_verified, profile_verified_at) |
| `v14-backfill-profile-completeness.sql` | Backfill profile_completeness for existing FREELANCERs |

Full index: see comments in `src/main/resources/sql/v4-update.sql`.

### Environment variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | MySQL user | root |
| `DB_PASSWORD` | MySQL password | (empty) |
| `JWT_SECRET` | JWT signing key | (dev default in yml) |
| `UPLOAD_DIR` | File upload root | ./uploads |
| `UPLOAD_BASE_URL` | Base URL for upload links | (empty) |
| `MAIL_*` | SMTP for OTP & notifications | (see application.yml) |
| `MPESA_*` or admin payment gateway | M-Pesa / payment config | (optional) |
| `app.rate-limit.login-per-minute` | Rate limit: login | 15 |
| `app.rate-limit.forgot-per-minute` | Rate limit: forgot-password | 5 |
| `app.rate-limit.upload-per-minute` | Rate limit: upload | 30 |

### 🔑 Default Test Accounts (auto-seeded)

| Role       | Email                      | Password      | Balance (TZS) |
|-----------|----------------------------|---------------|---------------|
| Admin     | admin@winga.co.tz          | Admin@1234    | 0             |
| Client    | amina@client.tz            | Client@1234   | 2,000,000     |
| Client    | john@client.tz             | Client@1234   | 5,000,000     |
| Freelancer| david@freelancer.tz        | Freelancer@1234 | 0           |
| Freelancer| fatuma@freelancer.tz       | Freelancer@1234 | 150,000     |

---

## 📡 Key API Endpoints

### Public (No Auth)
| Method | Endpoint               | Description               |
|--------|------------------------|---------------------------|
| POST   | `/api/auth/register`   | Register as Client/Freelancer |
| POST   | `/api/auth/login`      | Login → returns JWT       |
| GET    | `/api/jobs`            | Browse open jobs           |
| GET    | `/api/jobs/{id}`       | Job detail                |

### Wallet
| Method | Endpoint                        | Description              |
|--------|---------------------------------|--------------------------|
| GET    | `/api/wallet/balance`           | Check balance            |
| POST   | `/api/wallet/deposit/simulate`  | M-Pesa deposit simulation|
| POST   | `/api/wallet/withdraw`          | Withdraw funds           |
| GET    | `/api/wallet/transactions`      | Transaction history      |

### Escrow
| Method | Endpoint                          | Role       | Description          |
|--------|-----------------------------------|------------|----------------------|
| POST   | `/api/contracts/hire/{proposalId}`| CLIENT     | Hire & lock escrow   |
| POST   | `/api/contracts/{id}/submit`      | FREELANCER | Submit work          |
| POST   | `/api/contracts/{id}/approve`     | CLIENT     | Release payment      |
| POST   | `/api/contracts/{id}/dispute`     | Both       | Raise dispute        |

### Profile & workers (frontend alignment)

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/users/me` | My profile (includes `profileCompleteness`, `isProfileComplete`, `profileVerified`, `profileVerifiedAt`) |
| GET | `/api/users/me/profile-checklist` | Worker: `{ complete, profileCompleteness, missingFields[] }` for onboarding UI |
| PATCH | `/api/users/me` | Update profile; FREELANCER requires: fullName, country, headline, languages, paymentPreferences, workType, timezone (400 + message if missing) |
| GET | `/api/workers` | List workers (public). Query: `keyword`, `employmentType`, `language`, `skill`, `categoryId`, `profileVerified`, `profileComplete`, `sort` |
| POST | `/api/upload` | Upload file; `type=profile` or `type=cv`. Returns URL to set in profile. |

**Validation errors** (e.g. profile incomplete): `{ "success": false, "message": "Profile incomplete. Required fields: country, timezone, ..." }`.

### Admin: workers export & bulk actions

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/admin/export/workers?incompleteOnly=&withCvOnly=` | Export workers CSV (optional filters) |
| POST | `/api/admin/export/workers` | Export selected workers; body: `{ "userIds": [1,2,3] }` |
| PUT | `/api/admin/users/{id}/verify-profile?verified=` | Set worker profile verified (badge) |
| POST | `/api/admin/users/bulk-verify-profile` | Bulk verify/unverify; body: `{ "userIds": [1,2,3], "verified": true }` |
| GET | `/api/admin/analytics?from=&to=` | Jobs per category, proposals per job (top 50), revenue in period |
| GET | `/api/admin/export/contracts` | Export all contracts as CSV |

### Refresh token rotation
- **POST** `/api/auth/refresh` — Body: `{ "refreshToken": "..." }`. Returns new `accessToken` and new `refreshToken`. Use the new refresh token next time (rotation). Public endpoint.

### 2FA (optional, admin)
- TOTP (e.g. Google Authenticator) for admin accounts can be added later: store `totpSecret` per user, endpoints to enable/verify TOTP, and require TOTP code at admin login when enabled.

### Rate limiting (per IP)

| Path | Limit (default) |
|------|------------------|
| `POST /api/auth/login` | 15/minute |
| `POST /api/auth/forgot-password` | 5/minute |
| `POST /api/upload` | 30/minute |

When exceeded: **429 Too Many Requests** with `{ "success": false, "message": "Too many requests. Please try again later." }`.

---

## 🔌 WebSocket (Real-time)

Connect via SockJS to `/ws`

| Destination                         | Type      | Description                    |
|-------------------------------------|-----------|--------------------------------|
| `/topic/contract.{contractId}`      | Subscribe | Receive contract chat messages |
| `/user/queue/notifications`         | Subscribe | Personal notifications         |
| `/user/queue/messages`              | Subscribe | Direct messages                |
| `/app/chat/{contractId}`            | Send      | Send a chat message            |

---

## 🧪 Running Tests

```bash
./mvnw test
```

With JDK 21 (recommended if you see Lombok compile errors on JDK 24+):

```bash
./scripts/build-with-jdk21.sh test
```

## 🐳 Docker

```bash
# Build and run backend + MySQL
docker-compose up -d

# Backend: http://localhost:8080
# MySQL: localhost:3306 (winga_db, user winga, password winga)
```

Set `JWT_SECRET` (min 32 chars) in production. Run migrations manually or use `spring.jpa.hibernate.ddl-auto=update` for dev only.

---

*Built with ❤️ for Tanzania 🇹🇿 by the Winga Team*
