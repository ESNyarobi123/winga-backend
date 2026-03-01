# Winga — Database Design (OFM Style + Escrow)

Relational database (MySQL). Muundo umegawanyika katika **Modules 4 Kuu**: Auth & Identity, Job Board, Proposals & Contracts (Escrow), Wallet & Transactions.

---

## Miongozo ya Developer

- **Indexing:** Indexes kwenye `created_at`, `category`, `status` (na kadhalika) ili queries ziwe **blazing fast** kama OFM Jobs.
- **Concurrency:** Kwenye **wallets**, tumia **Pessimistic Locking** (`SELECT ... FOR UPDATE`) wakati wa ku-update balance ili kuzuia double spending na race conditions (M-Pesa).
- **JSON:** Tumia JSON column kwa **skills** (profiles) na **tags** (jobs) ili kurahisisha frontend na search bila join tables nyingi.

---

## Module 1: Watumiaji (Auth & Identity)

### Table: `users`

Table mama; M-Pesa integration fields ipo.

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| role | ENUM | NOT NULL | 'CLIENT', 'FREELANCER', 'ADMIN' |
| email | VARCHAR(100) | NOT NULL, UNIQUE | Verified |
| phone_number | VARCHAR(20) | | Muhimu kwa M-Pesa/Tigo Pesa |
| password_hash | VARCHAR(255) | NOT NULL | Bcrypt/Argon2 |
| full_name | VARCHAR(100) | NOT NULL | |
| verification_status | ENUM | NOT NULL | 'UNVERIFIED', 'PENDING', 'VERIFIED' (NIDA/simu) |
| profile_completeness | INT | DEFAULT 0 | 0–100% |
| profile_image_url | VARCHAR(500) | | |
| is_active | BOOLEAN | NOT NULL, DEFAULT true | |
| created_at | DATETIME | NOT NULL | **INDEX** |
| updated_at | DATETIME | | |

**Indexes:** `email` (UNIQUE), `role`, `phone_number`, `created_at`.

### Table: `profiles` (One-to-One with users)

‘CV’ ya mtu; skills kwa JSON ili search iwe rahisi.

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | NOT NULL, UNIQUE, FK → users.id | |
| headline | VARCHAR(200) | | Mfano: "Senior React Developer" |
| bio | TEXT | | Maelezo marefu |
| skills | JSON | | Array: ["Java", "Spring Boot", "MySQL"] |
| hourly_rate | DECIMAL(18,2) | | |
| portfolio_url | VARCHAR(500) | | |

**Index:** `user_id` (UNIQUE FK).

---

## Module 2: Kazi (Job Board — OFM Style)

Tags na filters zimepangwa; speed kwa created_at na category.

### Table: `jobs`

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| client_id | BIGINT | NOT NULL, FK → users.id | |
| title | VARCHAR(200) | NOT NULL | |
| description | TEXT | NOT NULL | |
| category | VARCHAR(100) | | **INDEX** (filter) |
| tags | JSON | | ['Remote', 'Urgent', 'Tsh'] — **INDEX** (optional: generated column) |
| budget_min | DECIMAL(18,2) | | Range kama OFM |
| budget_max | DECIMAL(18,2) | | |
| budget | DECIMAL(18,2) | NOT NULL | Single display/backward compat |
| type | ENUM | NOT NULL | 'FIXED_PRICE', 'HOURLY' |
| status | ENUM | NOT NULL | 'OPEN', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED' |
| experience_level | VARCHAR(50) | | JUNIOR, MID, SENIOR |
| deadline | DATE | | |
| view_count | BIGINT | NOT NULL, DEFAULT 0 | |
| created_at | DATETIME | NOT NULL | **INDEX** (sort Newest) |
| updated_at | DATETIME | | |

**Indexes:** `client_id`, `status`, **`created_at`**, **`category`**; optional: (status, category, created_at) composite.

---

## Module 3: Maombi & Mikataba (Escrow — Upwork Style)

### Table: `proposals`

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| job_id | BIGINT | NOT NULL, FK → jobs.id | |
| freelancer_id | BIGINT | NOT NULL, FK → users.id | |
| cover_letter | TEXT | NOT NULL | |
| bid_amount | DECIMAL(18,2) | NOT NULL | |
| estimated_duration | VARCHAR(100) | | |
| status | ENUM | NOT NULL | 'PENDING', 'SHORTLISTED', 'REJECTED', 'HIRED' |
| client_note | VARCHAR(1000) | | |
| created_at | DATETIME | NOT NULL | INDEX |
| updated_at | DATETIME | | |

**Unique:** (job_id, freelancer_id). **Indexes:** job_id, freelancer_id, status.

### Table: `contracts`

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| job_id | BIGINT | NOT NULL, FK → jobs.id | |
| proposal_id | BIGINT | NOT NULL, UNIQUE, FK → proposals.id | |
| client_id | BIGINT | NOT NULL, FK → users.id | |
| freelancer_id | BIGINT | NOT NULL, FK → users.id | |
| total_amount | DECIMAL(18,2) | NOT NULL | |
| escrow_amount | DECIMAL(18,2) | NOT NULL, DEFAULT 0 | Pesa iliyoshikiliwa |
| released_amount | DECIMAL(18,2) | NOT NULL, DEFAULT 0 | Pesa iliyokwisha lipwa |
| platform_fee_collected | DECIMAL(18,2) | NOT NULL, DEFAULT 0 | |
| status | ENUM | NOT NULL | 'ACTIVE', 'PAUSED', 'REVIEW_PENDING', 'DISPUTED', 'COMPLETED', 'TERMINATED' |
| termination_reason | TEXT | | |
| completed_at | DATETIME | | |
| created_at | DATETIME | NOT NULL | INDEX |
| updated_at | DATETIME | | |

**Indexes:** client_id, freelancer_id, status.

### Table: `milestones`

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| contract_id | BIGINT | NOT NULL, FK → contracts.id | |
| title | VARCHAR(200) | NOT NULL | |
| description | TEXT | | |
| amount | DECIMAL(18,2) | NOT NULL | |
| due_date | DATE | | |
| order_index | INT | NOT NULL, DEFAULT 0 | |
| status | ENUM | NOT NULL | PENDING, FUNDED, SUBMITTED, APPROVED |
| submission_note | TEXT | | |
| review_note | TEXT | | |
| funded_at | DATETIME | | |
| submitted_at | DATETIME | | |
| approved_at | DATETIME | | |
| created_at | DATETIME | NOT NULL | |
| updated_at | DATETIME | | |

**Indexes:** contract_id, status.

---

## Module 4: Pesa (Wallet & Transactions)

### Table: `wallets`

Kila user ana wallet. **Balance updates:** lazima kutumia **Pessimistic Lock** (SELECT ... FOR UPDATE) ili kuzuia race conditions.

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| user_id | BIGINT | NOT NULL, UNIQUE, FK → users.id | |
| balance | DECIMAL(18,2) | NOT NULL, DEFAULT 0 | |
| currency | ENUM | NOT NULL | 'TZS', 'USD' |
| total_earned | DECIMAL(18,2) | DEFAULT 0 | |
| total_spent | DECIMAL(18,2) | DEFAULT 0 | |
| last_updated_at | DATETIME | | |

**Concurrency:** Repository method `findByUserIdWithLock` → `SELECT ... FOR UPDATE` wakati wa deposit/withdraw/escrow.

### Table: `wallet_transactions` (transactions)

Rekodi ya kila miamala.

| Column | Type | Constraints | Notes |
|--------|------|-------------|--------|
| id | BIGINT | PK, AUTO_INCREMENT | |
| wallet_id | BIGINT | NOT NULL, FK → wallets.id | |
| type | ENUM | NOT NULL | 'DEPOSIT', 'WITHDRAWAL', 'ESCROW_LOCK', 'ESCROW_RELEASE', 'PLATFORM_FEE', 'REFUND' |
| amount | DECIMAL(18,2) | NOT NULL | |
| balance_before | DECIMAL(18,2) | NOT NULL | |
| balance_after | DECIMAL(18,2) | NOT NULL | |
| description | VARCHAR(500) | | |
| reference_id | VARCHAR(100) | | M-Pesa Ref, Contract ID, etc. |
| provider | VARCHAR(20) | | MPESA, TIGOPESA, AIRTEL |
| external_transaction_id | VARCHAR(50) | | |
| status | ENUM | NOT NULL | 'PENDING', 'COMPLETED', 'FAILED' |
| created_at | DATETIME | NOT NULL | **INDEX** |

**Indexes:** wallet_id, type, created_at.

---

## Tables Zingine (tayari kwenye backend)

- **chat_messages** — sender_id, receiver_id, contract_id, content, timestamp.
- **notifications** — user_id, type, title, message, is_read, created_at.
- **platform_revenue** — period, total_fees, total_transactions (optional).

---

## Muhtasari

| Kitu | Utekelezaji |
|------|-------------|
| Indexing | created_at, category, status kwenye jobs; created_at kwenye proposals, contracts, transactions. |
| Concurrency | `WalletRepository.findByUserIdWithLock()` → PESSIMISTIC_WRITE. |
| JSON | skills (profiles) → TEXT/JSON; tags (jobs) → VARCHAR comma-separated (au JSON). |
| Verification | users.verification_status (UNVERIFIED, PENDING, VERIFIED); profile_completeness. |
| Job budget range | budget_min, budget_max, type (FIXED_PRICE/HOURLY). |
| Transaction status | wallet_transactions.status (PENDING, COMPLETED, FAILED). |

---

## Migration (Backend)

- **JPA/Hibernate:** `spring.jpa.hibernate.ddl-auto: update` itaongeza columns mpya (profiles table, users.verification_status, users.profile_completeness, jobs.budget_min/budget_max/type, wallet_transactions.status, ContractStatus PAUSED) bila kubomoa data.
- **Production:** Badilisha kwa `validate` na tumia Flyway/Liquibase kwa migrations.
