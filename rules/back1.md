---
trigger: always_on
---

# Backend Blueprint: Freelance Marketplace (TZ Hybrid Edition)

## 1. Project Overview & Context
We are building a **Freelance Marketplace** tailored for the Tanzanian market.
* **Concept:** A hybrid of "OFM Jobs" (Clean, simple job board for viewing) and "Upwork" (Complex Escrow system, Bidding, and Chat for execution).
* **Goal:** To provide a secure platform where Clients post jobs, Freelancers bid, and payments are held in Escrow until work is approved.
* **Monetization:** The platform takes a commission (e.g., 10%) from the payment before releasing it to the freelancer.

---

## 2. Technical Stack (Strict Requirements)
* **Language:** Java 21 (LTS).
* **Framework:** Spring Boot 3.2+.
* **Build Tool:** Maven.
* **Database:** MySQL 8.0.
* **ORM:** Spring Data JPA (Hibernate).
* **Authentication:** Spring Security 6 + JWT (Stateless).
* **Real-time:** Spring WebSocket (STOMP) with SockJS fallback.
* **Utilities:**
    * `Lombok` (for boilerplate).
    * `MapStruct` (for Entity-DTO mapping).
    * `OpenAPI / Swagger` (for API Documentation).
* **Coding Style:**
    * Use **Constructor Injection** (`@RequiredArgsConstructor`).
    * Use **Records** for DTOs.
    * Always use `BigDecimal` for currency/money.
    * Implement Global Exception Handling (`@ControllerAdvice`).

---

## 3. Database Schema & Entity Relationships

### A. Users & Roles
* **Entity:** `User`
    * `id` (Long, PK), `email` (Unique), `passwordHash`, `fullName`, `phoneNumber` (Important for mobile money), `role` (Enum: CLIENT, FREELANCER, ADMIN), `profileImageUrl`, `isVerified` (Boolean), `createdAt`.
* **Entity:** `Wallet`
    * `id`, `user_id` (OneToOne), `balance` (BigDecimal, default 0.00), `currency` (Enum: TZS, USD).

### B. Jobs & Proposals ( The "Job Board" Aspect)
* **Entity:** `Job`
    * `id`, `client_id` (ManyToOne), `title`, `description`, `budget` (BigDecimal), `deadline` (Date), `status` (Enum: OPEN, IN_PROGRESS, COMPLETED, CANCELLED), `tags` (List<String>), `viewCount`.
* **Entity:** `Proposal`
    * `id`, `job_id` (ManyToOne), `freelancer_id` (ManyToOne), `coverLetter` (Text), `bidAmount` (BigDecimal), `estimatedDuration` (String), `status` (Enum: PENDING, SHORTLISTED, REJECTED, HIRED).
    * *Logic:* A freelancer can only submit one proposal per job.

### C. The Escrow Engine (The "Upwork" Aspect)
* **Entity:** `Contract`
    * `id`, `job_id`, `client_id`, `freelancer_id`, `proposal_id`, `totalAmount` (BigDecimal), `escrowAmount` (BigDecimal - funds currently locked), `releasedAmount` (BigDecimal), `status` (Enum: ACTIVE, DISPUTED, COMPLETED, TERMINATED), `createdAt`.
* **Entity:** `Milestone` (Optional but recommended)
    * `id`, `contract_id`, `description`, `amount`, `dueDate`, `status` (Enum: PENDING, FUNDED, COMPLETED, APPROVED).

### D. Communication
* **Entity:** `ChatMessage`
    * `id`, `contract_id` (Optional - allows pre-hire chat if needed), `sender_id`, `receiver_id`, `content`, `timestamp`, `isRead`.

---

## 4. Core Business Logic & Algorithms

### Algorithm 1: The Hiring & Escrow Locking Process
**Trigger:** Client clicks "Hire" on a Proposal.
1.  **Validation:** Check if Client's `Wallet.balance` >= `Proposal.bidAmount`.
    * *If Insufficient:* Throw `InsufficientFundsException`.
2.  **Locking Funds:**
    * Deduct `bidAmount` from Client's `Wallet`.
    * Create a `Contract` entity.
    * Set `Contract.escrowAmount` = `bidAmount`.
    * Set `Job.status` = `IN_PROGRESS`.
    * Set `Proposal.status` = `HIRED`.
3.  **Notification:** Send system notification to Freelancer: "You have been hired. Funds are secured in Escrow."

### Algorithm 2: Work Submission & Approval
**Trigger:** Freelancer submits work -> Client approves.
1.  **Submission:** Freelancer calls API to mark contract/milestone as "REVIEW_PENDING".
2.  **Approval (The Payout Logic):**
    * Client calls API `approveWork(contractId)`.
    * **Calculate Commission:** `platformFee` = `escrowAmount` * 0.10 (10%).
    * **Calculate Net Pay:** `freelancerPay` = `escrowAmount` - `platformFee`.
    * **Transfer:**
        * Add `freelancerPay` to Freelancer's `Wallet`.
        * Add `platformFee` to Admin's Revenue Table.
        * Set `Contract.escrowAmount` = 0.
        * Set `Contract.status` = `COMPLETED`.

### Algorithm 3: Mobile Money Integration (Simulation)
Since we are in Tanzania, we need a Simulation Endpoint for Deposits.
* **Endpoint:** `POST /api/v1/payments/deposit/simulate`
* **Input:** `userId`, `amount`, `phoneNumber`, `provider` (MPESA/TIGOPESA).
* **Logic:** Immediately credit the User's `Wallet` (Simulating a successful callback from a payment gateway).

---

## 5. API Endpoints Structure (RESTful)

### Public Endpoints (No Auth Required)
* `POST /api/auth/register` (Select Role: Client/Freelancer)
* `POST /api/auth/login` (Returns JWT)
* `GET /api/jobs` (List all open jobs - with pagination & filters)
* `GET /api/jobs/{id}` (View job details)

### Client Endpoints (Role: CLIENT)
* `POST /api/jobs` (Post a new job)
* `GET /api/client/proposals/{jobId}` (View who applied to my job)
* `POST /api/contracts/hire/{proposalId}` (Hire & Lock Escrow)
* `POST /api/contracts/{id}/approve` (Release funds)

### Freelancer Endpoints (Role: FREELANCER)
* `POST /api/proposals/{jobId}` (Apply for a job)
* `GET /api/freelancer/my-contracts` (View active jobs)
* `POST /api/contracts/{id}/submit` (Submit work for review)

### Wallet Endpoints (Authenticated)
* `GET /api/wallet/balance`
* `POST /api/wallet/deposit` (Simulate M-Pesa deposit)
* `POST /api/wallet/withdraw` (Request withdrawal)

---

## 6. Implementation Instructions for AI
1.  **Step 1:** Generate the Project Structure (pom.xml, Main class).
2.  **Step 2:** Generate the Entities and Repositories first.
3.  **Step 3:** Implement the `UserService` and `JwtAuthenticationFilter`.
4.  **Step 4:** Implement the `WalletService` (This is critical - handle concurrent transactions carefully).
5.  **Step 5:** Implement `JobService` and `ProposalService`.
6.  **Step 6:** Implement the `ContractService` containing the Escrow Logic.
7.  **Step 7:** Create Controllers and DTOs.