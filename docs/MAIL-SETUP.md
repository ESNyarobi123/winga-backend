# Mail setup (OTP & notifications)

Winga inatumia **winga@ericksky.online** kwa kutuma OTP na barua pepe za mfumo.

## SMTP (ericksky.online)

| Kipande | Thamani |
|--------|---------|
| Server | mail.ericksky.online |
| Username | winga@ericksky.online |
| Port | 587 (STARTTLS) au 465 (SSL) |
| Auth | Required (IMAP, POP3, SMTP) |

## Backend config

`application.yml` tayari iko na defaults:

- `spring.mail.host`: mail.ericksky.online  
- `spring.mail.port`: 587  
- `spring.mail.username`: winga@ericksky.online  
- `spring.mail.password`: default iko kwenye application.yml; unaweza kubadilisha kwa env `MAIL_PASSWORD` ikiwa unataka.

## Password

Default password iko tayari kwenye config (umekubali). OTP email inafanya kazi bila kuweka env. Kama unataka kutumia password tofauti, weka `MAIL_PASSWORD` kwenye environment.

## Port 465 (SSL)

Ikiwa unatumia port 465 badala ya 587:

```bash
export MAIL_PORT=465
```

Kwenye `application.yml` (au profile), ongeza:

```yaml
spring:
  mail:
    port: ${MAIL_PORT:587}
    properties:
      mail:
        smtp:
          ssl:
            enable: true
```

## Primary IP

Server ya mail: **109.123.240.147** (mail.ericksky.online).
