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
- Java 21
- Maven 3.9+
- MySQL 8.0+

### Setup

```bash
# 1. Create the database
mysql -u root -p -e "CREATE DATABASE winga_db;"

# 2. Configure credentials (or use environment variables)
# Edit src/main/resources/application.yml:
#   DB_USERNAME=root
#   DB_PASSWORD=yourpassword

# 3. Run the application
./mvnw spring-boot:run

# 4. Access Swagger UI
open http://localhost:8080/swagger-ui.html
```

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

---

*Built with ❤️ for Tanzania 🇹🇿 by the Winga Team*
