# Features: Milestones, Revisions, Add-on, Dispute (kama ulivyoomba)

Hii inaeleza **features 4** zilizoombwa na jinsi zilivyotekelezwa kwenye backend.

---

## 1. Mfumo wa Milestones (Hatua kwa Hatua) ✅

**Ombi:** Kazi igawanywe vipande; mteja akiisha kulipia Milestone 1, hawezi kurudi nyuma kusema "Badilisha design" bila kuanzisha Milestone mpya ya malipo.

**Utekelezaji:**
- **CLIENT** anaongeza milestones baada ya kuajiri: `POST /api/contracts/{id}/milestones` (title, amount, dueDate, orderIndex). Jumla ya milestones ≤ contract total.
- **FREELANCER** anatumia kazi ya milestone: `POST /api/contracts/{id}/milestones/{milestoneId}/submit?note=...` → status inaenda IN_REVIEW.
- **CLIENT** ana-approve milestone: `POST /api/contracts/milestones/{milestoneId}/approve` → malipo yanatolewa kwa freelancer, escrow inapungua.
- **Ulinzi:** Milestone iliyo-approve (APPROVED) haibadilishwi tena. Hakuna "request changes" kwa milestone tayari approved — ni kama "lock". Kama mteja anataka mabadiliko baada ya approve, anahitaji milestone mpya (add-on au new milestone).

---

## 2. Kikomo cha Marekebisho (Revision Limits) ✅

**Ombi:** Wakati freelancer anatuma ofa, anaweka "Revisions: 3". Mteja akibonyeza "Request Changes", system inahesabu 1/3, 2/3, 3/3. Mara ya 4 system inakataa: "Umefikisha kikomo... ongeza pesa (Create New Milestone)."

**Utekelezaji:**
- **Proposal:** `revisionLimit` (default 3). Freelancer anaweka kwenye ofa: `ProposalRequest.revisionLimit` (optional, 0–10).
- **Contract:** `revisionLimit` (inacopywa kutoka proposal) na `revisionsUsed` (0 at hire).
- **Request Changes:** `POST /api/contracts/{id}/request-changes?note=...` (CLIENT only). System inahesabu: ikiwa `revisionsUsed >= revisionLimit` → 400 na ujumbe: *"Umefikisha kikomo cha marekebisho ya bure (3/3). Tafadhali ongeza Milestone mpya (malipo ya ziada) ili uendelee kufanya mabadiliko."* Vinginevyo: `revisionsUsed++`, contract/milestone inarudishwa kwenye ACTIVE/PENDING ili freelancer atumie tena.
- **ContractResponse** ina `revisionLimit` na `revisionsUsed` ili frontend ionyeshe "2/3".

---

## 3. Kitufe cha Add-on (Malipo ya Ziada) ✅

**Ombi:** Kwenye chat, freelancer ana kitufe "Propose Extra Contract". Anaandika k.m. "SMS Feature", bei Tsh 50,000. Mteja anapata notification kama invoice; lazima "Accept & Deposit" ndipo freelancer aendelee.

**Utekelezaji:**
- **Entity:** `ContractAddOn` (contract, title, description, amount, status: PROPOSED | ACCEPTED | REJECTED | COMPLETED).
- **Contract:** `addonEscrowAmount` — pesa za add-ons zilizokubaliwa (zinashikiliwa hadi complete).
- **Freelancer:** `POST /api/contracts/{id}/add-ons` (title, description, amount) → status PROPOSED, mteja anapata notification.
- **Client:** `POST /api/contracts/{id}/add-ons/{addOnId}/accept` → debit wallet, `addonEscrowAmount` += amount, status ACCEPTED.  
  `POST .../reject` → status REJECTED.
- **Client:** `POST /api/contracts/{id}/add-ons/{addOnId}/complete` → release payment kwa freelancer (minus platform fee), `addonEscrowAmount` -= amount, status COMPLETED.
- **List:** `GET /api/contracts/{id}/add-ons` (contract parties only).

---

## 4. Usuluhishi wa Migogoro (Dispute Resolution Center) ✅

**Ombi:** Kitufe "File a Dispute", pesa escrow inashikiliwa. Admin anaweza kusoma Original Scope (makubaliano) na Chat Logs. Admin ana kitufe "Force Release Funds" (kwa freelancer au mteja).

**Utekelezaji:**
- **File a Dispute:** `POST /api/contracts/{id}/dispute?reason=...` (client au freelancer). Contract status → DISPUTED; pesa ziko escrow (hakuna mtu anayeweza approve/release).
- **Admin list:** `GET /api/admin/disputes` — orodha ya contracts zenye status DISPUTED.
- **Admin detail:** `GET /api/admin/disputes/{id}` — inarudisha **DisputeDetailResponse**: contract (scope = job + milestones), job description, na **recent chat messages** (latest 100). Admin anaweza kusoma Original Scope na Chat Logs.
- **Force Release:** `PATCH /api/admin/disputes/{id}/resolve` body `{ "releaseTo": "CLIENT" | "FREELANCER" }` — admin anachagua pesa ziende kwa nani (refund mteja au payout freelancer).

---

## Schema (mabadiliko)

- **proposals:** `revision_limit` INT DEFAULT 3  
- **contracts:** `revision_limit`, `revisions_used`, `addon_escrow_amount`  
- **contract_add_ons:** table mpya (contract_id, title, description, amount, status, accepted_at, completed_at, created_at, updated_at)

---

## Muhtasari

| Feature            | Status | API / Logic |
|--------------------|--------|-------------|
| Milestones (lock)  | ✅     | Add milestone, submit milestone, approve; approved = final. |
| Revision limits    | ✅     | revisionLimit on proposal/contract; request-changes; message when limit reached. |
| Add-on             | ✅     | Propose → Accept & Deposit → Complete (release). |
| Dispute center     | ✅     | File dispute; admin list + detail (scope + chat); resolve (force release). |
