# Auth Flow: Register & Login

## Register (mtumiaji mpya)

1. **Enter Email** → `POST /api/auth/send-otp` `{ "email": "..." }`
2. **Verify OTP** → `POST /api/auth/verify-otp` `{ "email": "...", "otp": "123456" }`  
   - Response: `requiresRegistration: true`, `registrationToken` (Bearer)
3. **Select Role** → Employer (CLIENT) au Seeker (FREELANCER)
4. **Provide Role-Specific Details** (optional) — industry, company name, full name  
   - **Aki-skip** → anaingia dashboard bila kujaza; anaweza kujaza baadaye kwenye profile.
5. **Complete** → `POST /api/auth/register/complete`  
   - Header: `Authorization: Bearer <registrationToken>`  
   - Body: `{ "role": "CLIENT"|"FREELANCER", "fullName": "optional", "industry": "optional", "companyName": "optional" }`  
   - Response: `accessToken`, `refreshToken`, `user` → **Dashboard**

---

## Login (mtumiaji aliyesajiliwa)

1. **Enter Email** → `POST /api/auth/send-otp` `{ "email": "..." }`
   - OTP inatumwa kwenye **email**. Kama user ana **namba ya simu (WhatsApp)** kwenye profile na Winga-otp iko configured, **OTP ile ile** inatumwa pia kwenye **WhatsApp**. User anaweza kuchukua code kutoka email au WhatsApp.
2. **Verify OTP** → `POST /api/auth/verify-otp` `{ "email": "...", "otp": "123456" }`  
   - Response: `requiresRegistration: false`, `auth: { accessToken, refreshToken, user }`
3. **Dashboard** (tumia `accessToken` kwa API)

---

## Muhtasari

| Step        | Register                    | Login        |
|------------|-----------------------------|-------------|
| 1          | Enter Email                 | Enter Email |
| 2          | Verify OTP                  | Verify OTP  |
| 3          | Select Role                 | —           |
| 4          | Role-Specific Details / Skip| —           |
| 5          | Dashboard                   | Dashboard   |

---

## WhatsApp OTP (login tu)

- **Registration:** OTP inatumwa **email tu** (mtumiaji mpya hapo hana account).
- **Login:** Kama user tayari ana **phone number** kwenye profile na `app.winga-otp.base-url` imewekwa, backend inatuma OTP kwenye **email** na **WhatsApp** (code moja). Mtumiaji anaweza kuangalia email au WhatsApp na kuweka OTP.
- Huduma ya WhatsApp: **Winga-otp** (Baileys, Node.js) — project tofauti; inaendesha kwenye URL (WINGA_OTP_BASE_URL, default http://localhost:3100).
