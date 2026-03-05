# Winga-otp

WhatsApp OTP sender for **Winga**. Inatumia [Baileys](https://github.com/WhiskeySockets/Baileys) (WhatsApp Web API). Backend ya Winga (Java) inaita huduma hii ili kutuma OTP kwenye WhatsApp ya user wakati wa **login** (pamoja na email).

**Faili:** `src/handle.js` — inashughulikia muunganisho wa WhatsApp na kuonyesha QR code. `src/index.js` — server ya HTTP (POST /send-otp).

## Jinsi inavyofanya kazi

- **Registration:** OTP inatumwa **email tu** (user hapo bado hana account).
- **Login:** OTP inatumwa **email** na **WhatsApp** (namba ya simu kwenye profile) — code moja, user anaweza kuchukua kutoka email au WhatsApp.

Backend inaita `POST /send-otp` na body: `{ "phone": "255712345678", "code": "123456", "expiryMinutes": 10, "appName": "Winga" }`.

## Mahali pa kuweka

- Hii repo iko ndani ya `winga-backend/winga-otp` (au weka kwenye repo tofauti ukipenda).
- Backend inaelekeza kwa URL ya Winga-otp kupitia `app.winga-otp.base-url` (au env `WINGA_OTP_BASE_URL`).

## Kuanza

```bash
cd winga-otp
npm install
npm start
```

- Mara ya kwanza utaona **QR code** kwenye terminal. Scan na WhatsApp yako (Linked Devices) ili ku-link namba ya “Winga OTP”.
- Session inahifadhiwa `auth_info/`. Runs zijazo inaungana bila QR. **QR tena:** futa folder `auth_info` kisha `npm start`.

Port default: **3100**. Weka env `PORT` ukipenda.

## API

| Method | Path        | Body | Description        |
|--------|-------------|------|--------------------|
| POST   | `/send-otp` | `{ "phone": "255712345678", "code": "123456", "expiryMinutes": 10, "appName": "Winga" }` | Tuma OTP kwenye WhatsApp |
| GET    | `/health`   | -    | Status (WhatsApp connected/disconnected) |

## Backend (winga-backend)

Weka URL ya Winga-otp:

- **application.yml:**  
  `app.winga-otp.base-url: http://localhost:3100`  
  au
- **Environment:**  
  `WINGA_OTP_BASE_URL=http://localhost:3100`

Kama `base-url` haijawekwa, OTP inatumwa **email tu** (WhatsApp haijazima).

## Baileys version

Tumia **6.6.0** (imewekwa kwenye package.json). Baileys 6.7.x ina bug ya statusCode 405 (Connection Failure) — QR haitoki. Usi-upgrade bila kujaribu.

## Production

- Tumia namba ya simu maalum kwa “Winga OTP” (si personal).
- Session ya Baileys weka kwenye path salama na backup.
- Kwa mzigo mkubwa, fikiria WhatsApp Business API (official) badala ya Baileys.
