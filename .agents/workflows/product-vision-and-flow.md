# Winga — Product Vision & Flow (OFM Jobs UI + Upwork Escrow)

## 1. Lengo la Mfumo

- **Muonekano na mtiririko**: Kama [OFM Jobs](https://www.ofmjobs.com) — rahisi, orodha ya kazi wazi, filters na tags, hakuna clutter; Login/Signup wazi.
- **Logic ya malipo**: Kama **Upwork / Guru** — **Escrow**: mteja anaweka pesa kwenye system, freelancer anafanya kazi, system inamlipa freelancer (pesa haitoki nje bila kukubaliwa).
- **Mazingira ya Tanzania**: TZS + USD; M-Pesa, Tigo Pesa, Airtel Money; chat ndani ya app (si nje); verification (Verified Freelancer/Client).

---

## 2. Muundo wa UI (Kama OFM Jobs)

| Kipengele | OFM Jobs | Winga (tunafanya) |
|-----------|----------|-------------------|
| **Orodha ya kazi (Feed)** | List view rahisi, kadi per job | List view; kadi ina: kichwa, malipo (TZS/USD), aina (Full-time/Part-time), nchi/bendera, tags |
| **Tags & categories** | Vitufe juu: Chatting, Marketing, Design, n.k. | Tags zinazolingana na soko la Tanzania: Graphics, Web Design, Uandishi, IT Support, Data Entry, Digital Marketing, n.k. |
| **Clutter** | Kidogo; matangazo na menu chache | Hakuna clutter; Login/Signup wazi; navigation wazi |
| **Filters** | Sort by date, category, employment type | Sort by date, category, budget (TZS/USD), experience level |

---

## 3. Maboresho kwa Tanzania (Zaidi ya OFM Jobs)

| Kipengele | Maelezo |
|-----------|---------|
| **Malipo** | TZS na USD; M-Pesa, Tigo Pesa, Airtel Money — deposit kwenye wallet, escrow, release kwa freelancer. |
| **Chat** | Chat **ndani ya app** (kama Upwork) ili malipo yabaki kwenye Escrow; mtu asitoe nje bila kukubaliwa. |
| **Verification** | "Verified Freelancer" / "Verified Client" (NIDA au namba ya simu iliyosajiliwa) ili kuongeza uaminifu. |

---

## 4. Logic ya Escrow (Kama Upwork/Guru) — Iliyochaguliwa

Hii **si** job board tu (posting fee + wasiliana nje). Ni:

1. **Mteja** anaweka pesa kwenye **wallet** (kupitia M-Pesa/Tigo/Airtel).
2. Anapohire freelancer, pesa inaingia **Escrow** (Contract + milestones).
3. **Freelancer** anafanya kazi; anaweza ku-submit milestone.
4. **Mteja** anakubali kazi → pesa inatolewa Escrow na kuenda kwenye wallet ya freelancer.
5. Freelancer anaweza **withdraw** kwenye M-Pesa/Tigo/Airtel.

Mfumo unalinda wote: mteja halipi kabla ya kukubali; freelancer anajua pesa iko secured.

---

## 5. Mtiririko wa Mfumo (User Flows)

### 5.1 Mteja (Client)

1. **Jisajili / Ingia** → Wallet inaundwa (balance 0).
2. **Ongeza pesa** → Deposit (M-Pesa/Tigo/Airtel) → balance inaongezeka.
3. **Tanga kazi** → Post job (kichwa, maelezo, budget TZS/USD, category, tags, deadline).
4. **Angalia proposals** → Orodha ya wasiliani waliotuma proposal; anachagua mmoja.
5. **Hire** → Anahire freelancer → pesa inaingia Escrow (contract + milestones).
6. **Chat** → Wasiliana na freelancer ndani ya app.
7. **Kubali kazi** → Approve milestone/work → pesa inatolewa Escrow na kuenda kwa freelancer.
8. (Optional) **Dispute** → Kama kuna tatizo, raise dispute (admin/support).

### 5.2 Freelancer

1. **Jisajili / Ingia** → Profile (skills, bio, verification).
2. **Tafuta kazi** → Browse feed; filter by category, budget, tags.
3. **Apply** → Tuma proposal (cover letter, bid amount, duration).
4. **Kama amechaguliwa** → Contract inaundwa; anapata notification.
5. **Chat** → Wasiliana na mteja ndani ya app.
6. **Fanya kazi & submit** → Submit work/milestone kwa review.
7. **Mteja anakubali** → Pesa inatolewa Escrow → inaingia wallet ya freelancer.
8. **Withdraw** → Toa pesa kwenye M-Pesa/Tigo/Airtel.

### 5.3 Public (Haijaingia)

- **Browse jobs** → Orodha ya kazi (read-only); filters na tags.
- **Angalia detail ya job** → Kichwa, maelezo, budget, category, deadline.
- **Login / Signup** → Kitufe wazi juu.

---

## 6. Ulinganisho na Backend (winga-backend)

| Flow | Backend (tayari / kukamilika) |
|------|-------------------------------|
| Auth | `AuthController` (register, login); JWT; `UserController` (/users/me). |
| Wallet & deposit | `WalletController`, `WalletService`; deposit (M-Pesa simulation); TZS. |
| Jobs | `JobController`, `JobService`; create, list, filter, get by ID. |
| Proposals | `ProposalController`, `ProposalService`; create, list by job. |
| Hire & Escrow | `ContractController` (hire, submit, approve); `Contract` = Escrow; milestones. |
| Chat | `ChatController`, `ChatService`; WebSocket config. |
| Payment callback | `PaymentController` (/api/payments/callback) kwa M-Pesa/gateway. |
| Verification | `User.isVerified` (na NIDA/phone later). |

---

## 7. Muhtasari wa Maamuzi

- **Logic ya malipo**: **Escrow** (kama Upwork/Guru) — pesa kwenye system, release baada ya approval.
- **UI/flow**: Rahisi kama OFM Jobs (feed, tags, filters, clean), na mapambo ya Tanzania (TZS/USD, mobile money, verification, in-app chat).
- **Backend**: Tayari ina misingi ya Escrow, wallet, chat, jobs, proposals; inabaki kufanya alignment na frontend na kukamilisha M-Pesa/Tigo/Airtel integration halisi.

---

*Ref: OFM Jobs — https://www.ofmjobs.com (UI inspiration). Upwork/Guru — Escrow logic.*
