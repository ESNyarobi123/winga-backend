# Database Setup — Winga Backend

Mwongozo wa kuandaa database (MySQL) kwa development na production.

---

## Kwanza nini?

### Chaguo 1: **Kuanza tu application** (inatosha kwa dev)

Backend inatumia **Hibernate** na `spring.jpa.hibernate.ddl-auto: update` (application.yml).

- **Hakuna haja ya kukimbia SQL yoyote kwanza.**
- Onesha MySQL (XAMPP / MySQL server).
- Start Spring Boot:
  ```bash
  ./mvnw spring-boot:run
  ```
- Hibernate ita:
  - Kutengeneza database `winga_db` ikiwa haipo (kwa sababu ya `createDatabaseIfNotExist=true` kwenye URL).
  - Kutengeneza **tables** zote kutoka entities (users, profiles, jobs, contracts, n.k.).
  - Ku**ongeza** columns mpya ikiwa entity imebadilika (k.m. `industry`, `company_name` kwenye `users`).
- **Haibomoi** data: haitaki drop tables wala columns zilizopo.

Kwa development, **hii inatosha** — hakikisha tu MySQL iko on na credentials (DB_USERNAME, DB_PASSWORD) ziko sawa.

---

### Chaguo 2: **Kukimbia schema manually** (fresh DB / reference)

Ikiwa unataka database tupu na schema iliyoelezwa kwa SQL (k.m. kwa team au backup):

1. Fungua MySQL (k.m. XAMPP → Start MySQL).
2. Tengeneza database (auacha script kuitengeneza):
   ```bash
   mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS winga_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
   ```
3. Import schema:
   ```bash
   mysql -u root -p winga_db < src/main/resources/sql/schema-winga.sql
   ```
   **Onyo:** `schema-winga.sql` ina **DROP** tables zilizopo. Backup data ikiwa unahitaji.

Baada ya hapo, unaweza kuanza application. Ikiwa entity ina columns zaidi kuliko SQL file, Hibernate (na `ddl-auto: update`) itaongeza columns hizo wakati wa startup.

---

## Tables (kutoka entities)

| Table | Entity | Maelezo |
|-------|--------|---------|
| users | User | Auth, profile (email, fullName, phoneNumber, bio, skills, industry, companyName) |
| profiles | Profile | CV / extended profile (headline, hourly_rate, portfolio_url) |
| wallets | Wallet | Balance, total_earned, total_spent |
| jobs | Job | Job board (title, budget, deadline, category, n.k.) |
| saved_jobs | SavedJob | Jobs walizohifadhi freelancers |
| proposals | Proposal | Proposals kwa jobs |
| contracts | Contract | Contracts baada ya hire |
| milestones | Milestone | Contract milestones |
| contract_add_ons | ContractAddOn | Add-ons kwa contract |
| reviews | Review | Reviews baada ya contract |
| chat_messages | ChatMessage | Chat (job + contract) |
| notifications | Notification | In-app notifications |
| wallet_transactions | WalletTransaction | Wallet history |
| platform_revenue | PlatformRevenue | Platform fee tracking |

---

## Production

- Badilisha `ddl-auto` kuwa **`validate`** (au **`none`**) ili Hibernate isiwezi kubadilisha schema.
- Tumia **Flyway** au **Liquibase** kwa migrations (kutengeneza/update tables kwa scripts zinazotrack).

---

## Config (application.yml)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/winga_db?createDatabaseIfNotExist=true&...
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
  jpa:
    hibernate:
      ddl-auto: update   # dev; production: validate au none
```

Weka `DB_USERNAME` na `DB_PASSWORD` ikiwa MySQL yako inahitaji.
