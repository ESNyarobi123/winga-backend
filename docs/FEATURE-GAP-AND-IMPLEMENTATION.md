# Winga — Feature Gap Analysis & Implementation Guide

Hati hii inalinganisha **features unazotaka** na **mfumo uliopo** (backend + Winga ui + admin-dashboard), kisha inatoa mwongozo wa **utekelezaji** kwa kila kipande kinachokosekana.

---

## Implemented (Recent)

- **Backend:** Commission 15%; User + Job location (city, region, lat/lng); Job attachments (URLs); Subscription entity + service (FREELANCER must have active subscription to bid); PortfolioItem + Certification entities + CRUD APIs; ReferralCode entity; Proposal moderation (PENDING_APPROVAL → APPROVED/REJECTED); ProposalResponse with price breakdown (providerFee, platformCommission, totalToClient); PlatformConfig API (GET /api/platform/config); SubscriptionController (GET /api/subscription/me); Admin: list proposals by moderationStatus, PATCH /admin/proposals/{id}/moderate.
- **Frontend (Winga ui):** i18n (Kiswahili default + English), language switch in navbar; Home: hero + core values banners + testimonials section; `BidPriceBreakdown` component; `platform.service.ts` for config; LocaleProvider + useT.
- **Admin dashboard:** Bids moderation page (list PENDING_APPROVAL proposals, Approve/Reject); nav: “Posts (Jobs)” and “Bids”; UI copy refresh.

---

## 1. Registration & User Types (Features 15 + 5 + 2)

| Feature | Hali sasa | Action |
|--------|-----------|--------|
| **Normal User (Mteja)** – kujisajili, kupost huduma, kupata vendors, hawezi kubid | ✅ **IPO**: Role `CLIENT`; OTP register; anaweza post job, angalia applicants, hire. Hawezi kubid. | — |
| **Service Provider (Vendor)** – special registration, **monthly subscription** ili kubid | ❌ **HAIPO**: Freelancer anaweza kujisajili bila subscription na kubid. Hakuna subscription model. | **Implement**: Subscription entity (plan, period, status); guard: FREELANCER anaweza kubid tu ikiwa ana active subscription; payment flow (M-Pesa/Tigo) kwa subscription. |
| **Auto category assignment** – on registration, system weka category (e.g. Plumbing → Plumbing) | ⚠️ **SEHEMU**: User ana `industry`, `skills`; Job ana `category`. Hakuna auto-assign category kwa freelancer on signup. | **Implement**: On complete registration (FREELANCER), map industry/skills → `JobCategory`; store default category on User au Profile. |
| **Location (city, region)** – kila user anaweka location | ❌ **HAIPO**: User ana `country` tu. Hakuna city/region. | **Implement**: Add `city`, `region` (or `district`) to User; optional lat/lng. API + UI. |
| **Geolocation (GPS)** – system inagundua GPS (app/browser) | ❌ **HAIPO**: Hakuna latitude/longitude wala geolocation. | **Implement**: Add `latitude`, `longitude` to User (optional); frontend: get from browser Geolocation API; store on profile update. |

**Priority**: 5 (must). **Backend**: Subscription table + service; User/Profile location fields; category assignment on registration. **Frontend**: Subscription plan choice & payment (FREELANCER); location fields + GPS capture; category prefill.

---

## 2. UI/UX & Localization (Features 3 + 4)

| Feature | Hali sasa | Action |
|--------|-----------|--------|
| **Clean & mobile-first** | ✅ **IPO**: Tailwind, responsive layouts (public, client, worker). | — |
| **Home: banners / slideshow** – core values, testimonies, benefits | ⚠️ **SEHEMU**: Landing ni hero tu (title + Browse Jobs / Sign In). Testimonials zipo kwenye `/results` (video + text). | **Implement**: Home page: add banners/slider (core values, benefits); move or duplicate testimonies on home; optional CMS/config for slide content. |
| **Lugha default: Kiswahili (100%)** | ❌ **HAIPO**: UI kwa English. Hakuna i18n. | **Implement**: i18n (e.g. next-intl / react-i18next); default locale `sw`; translate all UI strings. |
| **Language switch → English** | ❌ **HAIPO**: Hakuna language switcher. | **Implement**: Locale switcher (sw/en); full translation files for both. |

**Priority**: 4. **Frontend**: Banners/slideshow on home; i18n + Swahili default + English; language switch component.

---

## 3. Core Workflow: Posting, Bidding & Job Lifecycle (Features 6, 12, 8, 13)

| Feature | Hali sasa | Action |
|--------|-----------|--------|
| **Customer post huduma (Feature 12)** – multi-step: aina, maelezo, location, budget, deadline, attachments | ⚠️ **SEHEMU**: Post-job iko (AI vs Manual, single long form). Ina title, description, budget, deadline, category, skills, n.k. **Lakini**: si multi-step wazi; hakuna location ya job; hakuna attachments (picha/video). | **Implement**: Multi-step form (Step 1: type/category, Step 2: details + location, Step 3: budget + deadline, Step 4: attachments); add Job location (city/region/lat-lng); add Job attachments (table or JSON + upload). |
| **Service Provider anabid (Feature 6)** | ✅ **IPO**: Proposal (coverLetter, bidAmount, estimatedDuration, revisionLimit); hire from applicants. | — |
| **Bid Form (Feature 13)** – price breakdown: service fee, Winga Commission (15%), other charges, total | ❌ **HAIPO**: Proposal ina `bidAmount` tu. Hakuna breakdown (service fee, commission %, total to client). Commission backend ni **10%** (config). | **Implement**: Backend: commission rate 10%→**15%** ikiwa unataka 15%; DTO/response: add breakdown (providerFee, platformCommission, totalToClient); Frontend: show breakdown on bid submit & on job view. |
| **Full status flow (Feature 8)** – Requested → Vendor chosen → In progress → Completed → Payment released | ⚠️ **KARIBU**: Job: OPEN, PAUSED, IN_PROGRESS, COMPLETED, CANCELLED. Contract: ACTIVE, REVIEW_PENDING, COMPLETED, n.k. “Requested” ≈ OPEN; “Vendor chosen” ≈ hired (Contract created); “In progress” ≈ ACTIVE; “Completed” ≈ COMPLETED; “Payment released” ≈ milestone(s) approved, funds released. Naming na UX flow zinaweza kuwa wazi zaidi kwenye UI. | **Implement**: Map statuses to your labels; add “Payment released” as explicit state or message (e.g. when contract COMPLETED and all milestones approved); optional JobStatus/ContractStatus value “PAYMENT_RELEASED” ikiwa unataka. |

**Priority**: 5 (posting/bidding), 5 (lifecycle). **Backend**: Job location + attachments; commission 15% + breakdown in API. **Frontend**: Multi-step post form; location + attachments; bid form with breakdown; status labels (SW/EN).

---

## 4. Portfolio, Reviews & Trust (Features 10 + 11)

| Feature | Hali sasa | Action |
|--------|-----------|--------|
| **Portfolio** – picha, video, projects zilizokwisha | ⚠️ **SEHEMU**: Profile ina `portfolioUrl` (single link). Hakuna multiple items (images/videos/projects). | **Implement**: PortfolioItem entity (user_id, type: image/video/project, url, title, description, order); CRUD API; UI: gallery + add/edit. |
| **Certifications** – PDF au images | ❌ **HAIPO**: Hakuna certifications. | **Implement**: Certification entity (user_id, name, fileUrl, issuer, date); upload PDF/image; API + profile section. |
| **Reviews & Ratings (1–5 + comment)** baada ya kazi | ✅ **IPO**: Review entity (contract_id, reviewer, reviewee, rating 1–5, comment); API get reviews for user, rating summary. | — |

**Priority**: 4. **Backend**: PortfolioItem, Certification entities + repos + services + controllers. **Frontend**: Portfolio gallery; certifications section; reviews tayari zipo.

---

## 5. Back Office & Moderation (Feature 7)

| Feature | Hali sasa | Action |
|--------|-----------|--------|
| **Content review** – posts, bids, portfolios, reviews manual approval; zuia spam | ⚠️ **SEHEMU**: **Jobs** zina moderation (PENDING_APPROVAL → APPROVED/REJECTED). Admin: Moderation page (jobs). **Bids (proposals)**: Hakuna moderation. **Portfolios/Certifications**: Haipo bado. **Reviews**: Hakuna moderation. | **Implement**: (1) Keep job moderation. (2) Add moderation for proposals ikiwa unataka (e.g. ModerationStatus on Proposal). (3) Portfolio/Certification: add moderation status when you add those entities. (4) Review: optional flag/review queue for admin (e.g. hide until approved). |

**Priority**: 4. **Backend**: Extend moderation to proposals (optional); portfolio/certification moderation when added; review moderation optional. **Admin UI**: Tabs or pages for “Posts”, “Bids”, “Portfolios”, “Reviews” with approve/reject.

---

## 6. Monetization & Revenue (Feature 14 + 9)

| Feature | Hali sasa | Action |
|--------|-----------|--------|
| **Monthly subscriptions (service providers)** | ❌ **HAIPO**: Tazama 1. above. | Implement subscription plans + payments (see §1). |
| **Transaction commission 15%** | ⚠️ **IPO 10%**: `platform.commission-rate: 0.10`. | **Implement**: Set `commission-rate: 0.15`; update any hardcoded 10% in code; expose in API for bid breakdown. |
| **Payment facilitation margins** (TIGO, M-Pesa, Airtel) | ⚠️ **SEHEMU**: M-Pesa Daraja config iko; deposit/withdraw flow iko. Margins per agent hazijaweka wazi. | **Implement**: Config or DB for provider-specific margins; apply when calculating fees (optional). |
| **Google Ads** | ❌ **HAIPO**: Hakuna ad placement. | **Implement**: Frontend: ad slots (e.g. sidebar, between jobs); Google AdSense snippet (policy-compliant). |
| **Affiliate Program (Feature 9)** – referral links, commission, Winga Wallet, dashboard | ❌ **HAIPO**: Hakuna referral, affiliate, na wallet ya affiliates. | **Implement**: ReferralCode (user_id, code, used_by); AffiliatePayout (referral_id, amount, status); track signup/hire by referral; wallet or ledger for affiliate balance; payout to M-Pesa/wallet; admin/affiliate dashboard. |

**Priority**: 5 (subscription + commission), 4 (affiliate). **Backend**: Subscription; commission 15%; referral/affiliate entities + services. **Frontend**: Affiliate dashboard (my link, stats, earnings); admin: affiliate list + payouts.

---

## 7. Summary Table – Features: Exists vs Implement

| Group | Features | Zipo? | Zilizopo | Zote zinazohitaji kazi |
|-------|----------|-------|----------|-------------------------|
| Registration & Users | 2, 5, 15 | Sehemu | CLIENT/FREELANCER, OTP, post job, get vendors | Subscription (vendor), auto category, location, GPS |
| UI/UX & Language | 3, 4 | Sehemu | Clean UI, testimonials on /results | Home banners/slideshow, Kiswahili default, language switch |
| Posting & Bidding | 6, 12, 13 | Sehemu | Post job (single form), bid (proposal) | Multi-step post, location + attachments, bid price breakdown (15%) |
| Job Lifecycle | 8 | Karibu | Job + Contract statuses | Map to “Requested→Vendor chosen→In progress→Completed→Payment released” |
| Trust & Portfolio | 10, 11 | Sehemu | Reviews (1–5 + comment) | Portfolio (multi-item), Certifications |
| Back Office | 7 | Sehemu | Job moderation | Moderation for bids, portfolios, reviews (when added) |
| Money & Affiliate | 9, 14 | Sehemu | Commission 10%, wallet, M-Pesa | 15% commission, subscriptions, affiliate program, ads |

---

## 8. Implementation Order (Suggested)

1. **Must (Priority 5)**  
   - Subscription for Service Providers (entity + payment + guard on bid).  
   - Commission 15% + bid breakdown (backend + frontend).  
   - Location (User + Job): city, region; optional GPS.  
   - Multi-step post form + job location + job attachments.

2. **High (Priority 4)**  
   - Home banners/slideshow + core values/testimonials.  
   - i18n: Kiswahili default + English + language switch.  
   - Portfolio (multi-item) + Certifications.  
   - Auto category assignment on freelancer registration.  
   - Moderation: extend to bids (and later portfolios/reviews).  
   - Affiliate program (referral, commission, wallet, dashboard).

3. **Later**  
   - Payment facilitation margins (per provider).  
   - Google Ads placement.  
   - Explicit “Payment released” state/label in lifecycle.

Ukishataka, naweza kuanza na **kipande kimoja** (k.m. subscription model, location fields, au 15% commission + breakdown) na kuandika schema + API + mabadiliko ya UI step-by-step.
