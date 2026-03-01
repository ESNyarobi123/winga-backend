# Winga — Roles na Functions (kama OFM Jobs, Upwork, Guru)

Hii inaeleza **kila role** na **kazi zake** kwenye backend, ili kufanana na OFM Jobs, Upwork na Guru.

---

## CLIENT: Posts jobs, hires freelancers, pays money

**Kazi za CLIENT kwenye Upwork/Guru/OFM:**
- Kuweka kazi (post job)
- Kuona applicants na kuchagua freelancer
- Kuajiri freelancer na kufunga pesa kwenye escrow
- Kulipa (approve work / release payment) au kukataa na kuomba mabadiliko
- Milestones: kugawa malipo kwa vipande na ku-approve kila milestone
- Kuongea na freelancer (chat)
- Kuweka review baada ya mwisho
- Ku-deposit pesa kwenye wallet (M-Pesa)
- Kuona jobs zake, contracts, wallet balance na history

---

### CLIENT — Functions na API (Winga backend)

| # | Function (kama OFM/Upwork/Guru) | Winga API | Maelezo |
|---|---------------------------------|-----------|---------|
| **1** | **Post job** | `POST /api/jobs` | Tuma kazi (title, description, budget, category, deadline, tags). CLIENT only. |
| **2** | **Edit job** | `PUT /api/jobs/{id}` | Badilisha kazi iliyo OPEN. Owner only. |
| **3** | **Cancel job** | `DELETE /api/jobs/{id}` | Ghairi kazi (si in progress). Owner only. |
| **4** | **List my jobs** | `GET /api/jobs/my-jobs` | Orodha ya jobs ulizoweka. CLIENT only. |
| **5** | **Browse jobs** (public) | `GET /api/jobs` | Tazama kazi zote wazi (search, category, budget). |
| **6** | **View job details** | `GET /api/jobs/{id}` | Tazama kazi moja (view count inaongezeka). |
| **7** | **View applicants** | `GET /api/proposals/jobs/{jobId}/applicants` | Orodha ya maombi (proposals) kwa job yako. CLIENT only. |
| **7b** | **Chat with applicant** (kabla ya kuajiri) | `POST /api/chat/jobs/{jobId}/messages?receiverId=`, `GET /api/chat/jobs/{jobId}/messages?otherUserId=` | Ongea na mwombaji; WebSocket: subscribe `/topic/job.{jobId}` au `/user/queue/messages`, send `/app/chat/job/{jobId}` (body: content, receiverId). |
| **8** | **Shortlist / Reject proposal** | `PATCH /api/proposals/{proposalId}/status?status=SHORTLISTED\|REJECTED` | Badilisha status ya proposal. CLIENT only. |
| **9** | **Hire freelancer** (kumkubali mmoja) | `POST /api/contracts/hire/{proposalId}` | Ajiri freelancer: pesa zinakwenda escrow, contract inaanza. CLIENT only. |
| **10** | **View my contracts** | `GET /api/contracts/client/my-contracts` | Orodha ya contracts ulizounda. CLIENT only. |
| **11** | **View contract details** | `GET /api/contracts/{id}` | Tazama contract (client au freelancer wa contract). |
| **12** | **Approve work (release payment)** | `POST /api/contracts/{id}/approve` | Thibitisha kazi → pesa zinachukuliwa escrow na kupata freelancer. CLIENT only. |
| **13** | **Add milestone** | `POST /api/contracts/{id}/milestones` | Ongeza milestone (title, amount, dueDate, orderIndex). Jumla ya milestones ≤ contract total. CLIENT only. |
| **14** | **Approve milestone** | `POST /api/contracts/milestones/{milestoneId}/approve` | Approve milestone moja → release payment ya milestone. CLIENT only. |
| **15a** | **End contract** (client) | `POST /api/contracts/{id}/terminate` | Mteja akomesha contract; pesa zilizobaki (escrow + addon) zinarudishwa kwa mteja, job inarudishwa OPEN. CLIENT only. |
| **15** | **Raise dispute** | `POST /api/contracts/{id}/dispute?reason=...` | Fungua dispute; admin atasimamia. |
| **16** | **Leave review** | `POST /api/contracts/{id}/reviews` | Weka rating na comment baada ya contract completed. |
| **17** | **Chat with freelancer** (baada ya hire) | `POST /api/chat/contracts/{contractId}/messages`, WebSocket `/app/chat/contract/{contractId}` | Tuma ujumbe kwenye contract chat. Subscribe `/topic/contract.{contractId}`. |
| **18** | **Read messages** | `GET /api/chat/contracts/{contractId}/messages`, `GET /api/chat/jobs/{jobId}/messages?otherUserId=` | Tazama historia (contract au job chat). |
| **19** | **Mark messages read** | `POST /api/chat/contracts/{contractId}/read`, `POST /api/chat/jobs/{jobId}/read` | Weka ujumbe kama usomwa. |
| **20** | **Wallet balance** | `GET /api/wallet/balance` | Tazama balance ya wallet. |
| **21** | **Deposit (M-Pesa)** | `POST /api/wallet/deposit/initiate` | Anzisha STK push (au simulate). |
| **22** | **Deposit simulate** | `POST /api/wallet/deposit/simulate` | Simulate deposit (testing). |
| **23** | **Transaction history** | `GET /api/wallet/transactions` | Historia ya transactions. |
| **24** | **Profile (me)** | `GET /api/users/me`, `PATCH /api/users/me` | Tazama/update profile. |
| **25** | **Saved jobs** (optional) | `GET /api/jobs/saved`, `POST /api/jobs/{id}/save`, `DELETE /api/jobs/{id}/save` | Bookmark jobs (CLIENT pia anaweza save). |
| **26** | **Notifications** | `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `GET /api/notifications/unread-count` | Tazama na mark read. |
| **27** | **Upload file** | `POST /api/upload?type=job` | Pakia file (e.g. attachment kwa job). |

---

### CLIENT — Security (backend)

- **POST /api/jobs** → `hasRole('CLIENT')`
- **PUT/DELETE /api/jobs/** → `hasRole('CLIENT')` + ownership in service
- **GET /api/jobs/my-jobs** → `hasRole('CLIENT')`
- **GET /api/proposals/jobs/{jobId}/applicants** → `hasRole('CLIENT')` + job owner
- **PATCH /api/proposals/{id}/status** → `hasRole('CLIENT')` + job owner
- **POST /api/contracts/hire/** → `hasRole('CLIENT')`
- **POST /api/contracts/{id}/milestones** → `hasRole('CLIENT')` (add milestone)
- **POST /api/contracts/milestones/{id}/approve** → `hasRole('CLIENT')` + contract client
- **GET /api/contracts/client/my-contracts** → `hasRole('CLIENT')`
- **POST /api/contracts/{id}/approve** → `hasRole('CLIENT')` + contract client

---

### CLIENT — Flow (muhtasari)

1. **Register/Login** → role = CLIENT  
2. **Deposit** → wallet balance inaongezeka (M-Pesa au simulate)  
3. **Post job** → job inaonekana kwenye board  
4. **Applicants** → proposals zinakuja; client anaweza shortlist/reject  
5. **Hire** → client anachagua proposal → POST hire → escrow inafungwa, contract inaanza  
6. **Work** → freelancer anafanya kazi, anaweza submit  
7. **Approve** → client ana-approve (au dispute) → pesa zinatoa kwa freelancer  
8. **Review** → mwisho wa contract, client anaweka review kwa freelancer  
9. **Chat** → job chat na applicants; baada ya hire contract chat  

---

## FREELANCER: Find work, apply, deliver, get paid

**Kazi za FREELANCER kwenye Upwork/Guru/OFM:**
- Kutafuta kazi (browse jobs)
- Kuomba kazi (submit proposal na revision limit)
- Kuongea na mteja kabla ya kuajiriwa (job chat)
- Baada ya kuajiriwa: submit work, submit milestone work, propose add-ons
- Kupokea malipo (approve → release to wallet)
- Ku-withdraw pesa
- Kuona profile, reviews, notifications

---

### FREELANCER — Functions na API (Winga backend)

| # | Function | Winga API | Maelezo |
|---|----------|-----------|---------|
| **1** | **Browse jobs** | `GET /api/jobs`, `GET /api/jobs/categories` | Tazama kazi wazi (search, category, budget). |
| **2** | **View job** | `GET /api/jobs/{id}` | Tazama maelezo ya kazi. |
| **3** | **Save job** (bookmark) | `POST /api/jobs/{id}/save`, `GET /api/jobs/saved`, `DELETE /api/jobs/{id}/save` | Weka/ondoa kwenye saved. |
| **4** | **Submit proposal** | `POST /api/proposals/jobs/{jobId}` | Tuma ofa (coverLetter, bidAmount, estimatedDuration, revisionLimit). FREELANCER only. |
| **5** | **My proposals** | `GET /api/proposals/my-proposals` | Orodha ya proposals ulizotuma. FREELANCER only. |
| **6** | **Chat with client** (kabla ya hire) | `POST /api/chat/jobs/{jobId}/messages?receiverId=`, `GET /api/chat/jobs/{jobId}/messages?otherUserId=` | Ongea na mteja (mwombaji). WebSocket `/app/chat/job/{jobId}`. |
| **7** | **My contracts** | `GET /api/contracts/my-contracts` | Orodha ya contracts ulizoajiriwa. FREELANCER only. |
| **8** | **View contract** | `GET /api/contracts/{id}` | Tazama contract. |
| **9** | **Submit work** | `POST /api/contracts/{id}/submit?note=...` | Tumia kazi kwa review (contract nzima). FREELANCER only. |
| **10** | **Submit milestone** | `POST /api/contracts/{id}/milestones/{milestoneId}/submit?note=...` | Tumia kazi ya milestone. FREELANCER only. |
| **11** | **Propose add-on** | `POST /api/contracts/{id}/add-ons` | Pendekeza kazi ya ziada (title, amount). Mteja Accept & Deposit. FREELANCER only. |
| **12** | **List add-ons** | `GET /api/contracts/{id}/add-ons` | Tazama add-ons za contract. |
| **13** | **Raise dispute** | `POST /api/contracts/{id}/dispute?reason=...` | Fungua dispute. |
| **14** | **Leave review** | `POST /api/contracts/{id}/reviews` | Weka rating kwa mteja (contract completed). |
| **15** | **Contract chat** | `POST /api/chat/contracts/{contractId}/messages`, WebSocket `/app/chat/contract/{contractId}` | Ongea na mteja baada ya hire. |
| **16** | **Wallet balance** | `GET /api/wallet/balance` | Tazama balance. |
| **17** | **Withdraw** | `POST /api/wallet/withdraw` | Toa pesa (mobile money). |
| **18** | **Transaction history** | `GET /api/wallet/transactions` | Historia ya transactions. |
| **19** | **Profile** | `GET /api/users/me`, `PATCH /api/users/me` | Tazama/update profile. |
| **20** | **My reviews** | `GET /api/users/{id}/reviews`, `GET /api/users/{id}/rating` | Reviews ulizopokea (profile). |
| **21** | **Notifications** | `GET /api/notifications`, `PATCH /api/notifications/{id}/read`, `GET /api/notifications/unread-count` | Tazama na mark read. |
| **22** | **Upload file** | `POST /api/upload?type=proposal` | Pakia file (e.g. attachment). |

---

### FREELANCER — Security (backend)

- **POST /api/proposals/** → `hasRole('FREELANCER')`
- **GET /api/proposals/my-proposals** → `hasRole('FREELANCER')`
- **GET /api/contracts/my-contracts** → `hasRole('FREELANCER')`
- **POST /api/contracts/{id}/submit** → `hasRole('FREELANCER')` + contract freelancer
- **POST /api/contracts/{id}/milestones/{mid}/submit** → `hasRole('FREELANCER')` + contract freelancer
- **POST /api/contracts/{id}/add-ons** → `hasRole('FREELANCER')` + contract freelancer

---

### FREELANCER — Flow (muhtasari)

1. **Register/Login** → role = FREELANCER  
2. **Browse & save jobs** → GET /api/jobs, save jobs  
3. **Apply** → POST proposal (revisionLimit), optional job chat na mteja  
4. **Hired** → mteja anachagua proposal → contract inaanza  
5. **Deliver** → submit work au submit milestone  
6. **Client** → approve (au request changes, count revision)  
7. **Get paid** → pesa zinatoa wallet; withdraw  
8. **Review** → weka review kwa mteja; tazama reviews zako (profile)  

---

## ADMIN: Platform management & disputes

**Kazi za ADMIN:**
- Kuona stats (users, jobs, contracts, revenue)
- Kusimamia users (verify/unverify)
- Kusuluhisha migogoro (disputes): kuona scope + chat, force release kwa client au freelancer

---

### ADMIN — Functions na API (Winga backend)

| # | Function | Winga API | Maelezo |
|---|----------|-----------|---------|
| **1** | **List users** | `GET /api/admin/users` | Orodha ya users (paginated). ADMIN only. |
| **2** | **Verify user** | `POST /api/admin/users/{id}/verify?verify=true\|false` | Weka isVerified + verificationStatus. ADMIN only. |
| **3** | **Platform stats** | `GET /api/admin/stats` | totalUsers, jobs, contracts, disputedContracts, totalPlatformRevenue. ADMIN only. |
| **4** | **List disputes** | `GET /api/admin/disputes` | Orodha ya contracts zenye status DISPUTED. ADMIN only. |
| **5** | **Dispute detail** | `GET /api/admin/disputes/{id}` | Original scope (contract + job + milestones) + chat logs (recent 100). ADMIN only. |
| **6** | **Resolve dispute** | `PATCH /api/admin/disputes/{id}/resolve` | Body `{ "releaseTo": "CLIENT" \| "FREELANCER" }`. Force release escrow. ADMIN only. |

---

### ADMIN — Security (backend)

- **/api/admin/** → `hasRole('ADMIN')` (SecurityConfig + @PreAuthorize on AdminController)
