# Ideas za Backend — Upande wa Mtafutaji Kazi (Freelancer)

Hati hii inaelezea **mapendekezo ya backend** kulingana na flow na majukumu ya Freelancer (OFM-style + Upwork-power). Kila eneo lina: **tayari**, **kukosekana**, na **mapendekezo maalum**.

---

## 1. Eneo la Wasifu (Profile — "CV Yake")

### Tayari
- **User**: `profileImageUrl`, `bio`, `skills` (string), `isVerified`, `verificationStatus`
- **UpdateProfileRequest**: `fullName`, `phoneNumber`, `bio`, `skills`, `profileImageUrl`
- **UserResponse**: `profileImageUrl`, `bio`, `isVerified`, `verificationStatus` — **hakuna `skills` wala `headline`**
- **Profile** entity (tabeli `profiles`): `headline`, `bio`, `skills`, `hourlyRate`, `portfolioUrl` — **haijatumika kwenye API** (UserService inatumia User tu)

### Kinachokosekana / Kupendekeza

| Kipengele | Pendekezo la Backend |
|-----------|----------------------|
| **Headline** | Ongeza `headline` (String, max 200) kwenye **User** au tumia **Profile** kwa freelancers. Kama unatumia Profile: `GET/PATCH /api/users/me/profile` (headline, portfolioUrl, skills) na kuhusisha na User. |
| **Portfolio (picha za kazi)** | **Option A:** Ongeza `portfolioUrl` (URL moja) kwenye User au Profile. **Option B (nzuri zaidi):** Entity `PortfolioItem` (userId, imageUrl, title, sortOrder); `POST/GET/DELETE /api/users/me/portfolio` ili freelancer aweke picha nyingi. |
| **Skills kwenye response** | Ongeza `skills` (na kwa hiari `headline`) kwenye **UserResponse** ili frontend ionyeshe "tags" kwenye kadi ya wasifu. |
| **Verification (NIDA / simu → Blue Tick)** | (a) **Submit for verification:** `POST /api/users/me/verification-request` — body: `documentType` (NIDA, PHONE), optional `documentImageUrl`; set `verificationStatus = PENDING`. (b) Admin tayari ana `PATCH /api/admin/users/{id}/verify`. (c) Orodha ya "verified skills/categories" kwa search: mtu anaweza ku-search "verified freelancers" — filter `User.isVerified = true`. |

### Muundo wa API (suggested)

```
GET  /api/users/me              → UserResponse (ongeza skills, headline)
PATCH /api/users/me             → UpdateProfileRequest (ongeza headline)
GET  /api/users/me/profile      → FreelancerProfileResponse (headline, skills[], portfolioItems[])  [kama utatumia Profile]
POST /api/users/me/portfolio    → PortfolioItem (imageUrl, title)  [kama utaongeza PortfolioItem]
GET  /api/users/me/portfolio   → List<PortfolioItemResponse>
DELETE /api/users/me/portfolio/{id}
POST /api/users/me/verification-request  → { documentType, documentImageUrl? }
```

---

## 2. Eneo la Kutafuta Kazi (Job Discovery — OFM Style)

### Tayari
- **Search & filter:** `GET /api/jobs?keyword=&category=&minBudget=&maxBudget=` (JobRepository.searchJobs)
- **Saved jobs (bookmark):** `GET /api/jobs/saved`, `POST /api/jobs/{id}/save`, `DELETE /api/jobs/{id}/save`
- **Categories:** `GET /api/jobs/categories`

### Kinachokosekana / Kupendekeza

| Kipengele | Pendekezo la Backend |
|-----------|----------------------|
| **"Kazi za Leo tu"** | Ongeza param `postedSince` (date au enum: TODAY, THIS_WEEK) kwenye `GET /api/jobs`. JobRepository: `AND j.createdAt >= :since`. |
| **"Bajeti juu ya Laki 1"** | Tayari: `minBudget`, `maxBudget` — hakiki frontend inatumia. |
| **"Remote"** | Ongeza `locationType` (REMOTE, ONSITE, HYBRID) kwenye **Job** entity na kwenye JobRequest/JobResponse; filter `GET /api/jobs?locationType=REMOTE`. |
| **Alerts (notification kazi ya fani yake)** | (a) Entity **JobAlert** (userId, category au tags/skills, isActive). (b) Job created → service inatafuta freelancers wenye alert zinazolingana (category/skills) → `notificationService.notify(..., JOB_POSTED)`. (c) `POST/GET/DELETE /api/freelancer/job-alerts` kuset alerts. |

### Muundo wa API (suggested)

```
GET /api/jobs?keyword=&category=&minBudget=&maxBudget=&postedSince=TODAY|THIS_WEEK&locationType=REMOTE
POST /api/freelancer/job-alerts   → { category?, tags? }  (create/update alert)
GET  /api/freelancer/job-alerts   → List<JobAlertResponse>
DELETE /api/freelancer/job-alerts/{id}
```

---

## 3. Eneo la Kuomba Kazi (Bidding & Proposals — Upwork Logic)

### Tayari
- **ProposalRequest:** `coverLetter`, `bidAmount`, `estimatedDuration`, `revisionLimit`
- **Proposal** entity: coverLetter, bidAmount, estimatedDuration, revisionLimit
- **Logic:** Hakuna "Connects" — kuomba bure; kamisi 10% inakatwa ukishalipa (ContractService).

### Kinachokosekana / Kupendekeza

| Kipengele | Pendekezo la Backend |
|-----------|----------------------|
| **Attachments (mfano wa kazi)** | Ongeza `attachmentUrls` (List&lt;String&gt;) kwenye **Proposal** (column JSON au separate table ProposalAttachment). ProposalRequest: `List<String> attachmentUrls`. ProposalResponse: `attachmentUrls`. |
| **Validation** | Bid amount: optional max (mfano si zaidi ya 2× job.budget); estimatedDuration max length. |

### Muundo (suggested)

- **Proposal:** `attachmentUrls` — column `TEXT` store JSON array `["url1","url2"]` au table `proposal_attachments(proposal_id, url)`.
- **ProposalRequest:** `attachmentUrls` (optional, max 5 URLs).
- **ProposalResponse:** `attachmentUrls`.

---

## 4. Eneo la Kazi (My Jobs & Workspace)

### Tayari
- **Active contracts:** `GET /api/freelancer/my-contracts` (na `/api/contracts/my-contracts`)
- **Chat:** ChatController + WebSocket; ChatMessage ina `attachmentUrl`
- **Submit work:** `POST /api/contracts/{id}/submit`, `POST /api/contracts/{id}/milestones/{milestoneId}/submit`

### Kinachokosekana / Kupendekeza

- Hakuna kubwa; workflow tayari iko. Unaweza kuongeza:
  - **GET /api/freelancer/dashboard** — tayari: balance, totalEarned, activeContractsCount, pendingProposalsCount.
  - **Optional:** `GET /api/contracts/{id}/timeline` — events (created, work submitted, approved, etc.) kwa frontend timeline.

---

## 5. Eneo la Pesa (Earnings & Wallet)

### Tayari
- **Escrow view:** **ContractResponse** ina `escrowAmount`, `releasedAmount`, `totalAmount` — frontend inaweza kuonyesha "Funds in Escrow" kwa kila contract.
- **Balance:** `GET /api/wallet/balance` → `WalletResponse` (balance, totalEarned, totalSpent, currency).
- **Withdraw:** `POST /api/wallet/withdraw` (WithdrawRequest: amount, phoneNumber, provider).

### Kinachokosekana / Kupendekeza

| Kipengele | Pendekezo la Backend |
|-----------|----------------------|
| **Escrow summary (jumla)** | **GET /api/freelancer/escrow-summary** → `{ totalInEscrow, byContract: [{ contractId, jobTitle, escrowAmount }] }` — jumla ya pesa zilizo escrow kwa mtu yote. |
| **Transaction history** | Tayari: `GET /api/wallet/transactions` (paginated). |

### Muundo (suggested)

```
GET /api/freelancer/escrow-summary  → { totalInEscrow, contracts: [...] }
```

---

## 6. Muhtasari wa Comparison (Backend)

| Kipengele | OFM (reference) | System yako (current) | Pendekezo |
|-----------|------------------|------------------------|-----------|
| Profile | Nyepesi | User: bio, skills; hakuna headline/portfolio kwenye API | Ongeza headline; portfolio (URL au PortfolioItem); verification-request API |
| Job search | Filters | keyword, category, min/max budget | postedSince, locationType (Remote); job alerts |
| Save job | - | ✅ Saved jobs (bookmark) | - |
| Proposal form | - | coverLetter, bidAmount, duration, revisionLimit | Ongeza attachmentUrls |
| Connects | - | Bure (no fee) | - |
| Escrow view | - | ContractResponse.escrowAmount | + GET escrow-summary jumla |
| Withdraw | - | ✅ M-Pesa/Tigo simulate + withdraw | - |
| Chat | - | ✅ In-app + attachmentUrl | - |

---

## 7. Prioritization (Backend)

**Phase 1 (haraka, high impact)**  
1. UserResponse: ongeza `skills` (na `headline` kama utaongeza kwenye User).  
2. Job search: `postedSince`, `locationType` (Job + filter).  
3. Proposal: `attachmentUrls` (list).  
4. GET `/api/freelancer/escrow-summary`.

**Phase 2**  
5. Portfolio: `PortfolioItem` entity + CRUD `/api/users/me/portfolio`.  
6. Verification request: `POST /api/users/me/verification-request`.  
7. Job alerts: entity + notification on job create + `/api/freelancer/job-alerts`.

**Phase 3**  
8. "Verified only" filter kwenye job applicants (client side) au search freelancers by skills + verified.

---

*Hati hii inarejelea mfumo wa Tanzania (OFM + Upwork hybrid); backend ideas zimeunganishwa na flow na roles ulizochambua.*
