# Database Connection — Winga Backend

Hii ni mahali pa **kuongeza au kubadilisha** maelezo ya database (URL, username, password) kwa kila mazingira.

---

## 1. Vigezo vya database (variables)

| Variable | Maelezo | Default (dev) | Mahali pa kuweka |
|----------|---------|----------------|------------------|
| **DB_USERNAME** | MySQL username | `root` | application.yml / env / .env |
| **DB_PASSWORD** | MySQL password | tupu (XAMPP) | application.yml / env / .env |
| **SPRING_DATASOURCE_URL** | JDBC URL (host, port, db name) | localhost:3306/winga_db | application.yml / env (Docker) |

---

## 2. Mahali pa kuweka / kuupdate

### A. Development (laptop / XAMPP / MySQL local)

**Faili:** `src/main/resources/application.yml`

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/winga_db?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Africa/Dar_es_Salaam
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:}
```

- **Kubadilisha URL:** badilisha `localhost:3306/winga_db` ikiwa MySQL iko kwenye host/port tofauti, au jina la database tofauti.
- **Kubadilisha user/password:** weka environment variables kabla ya kuanza app:
  ```bash
  export DB_USERNAME=root
  export DB_PASSWORD=your_mysql_password
  ./mvnw spring-boot:run
  ```
  Au weka password moja kwa moja kwenye `application.yml` (si salama kwa production):
  ```yaml
  password: ${DB_PASSWORD:your_mysql_password}
  ```

### B. Docker (deploy)

**Faili:** `.env` kwenye folder ile ile na `docker-compose.yml` (k.m. `docs/deploy/` au root ya repo).

Weka values hizi (bure kubadilisha kwa staging/production):

```env
# Database — MySQL container
MYSQL_ROOT_PASSWORD=StrongRootPass123!
MYSQL_DATABASE=winga_db
MYSQL_USER=winga
MYSQL_PASSWORD=WingaDbPass456!

# Backend inatumia MYSQL_USER / MYSQL_PASSWORD kama DB_USERNAME / DB_PASSWORD
# URL inajengwa na docker-compose: jdbc:mysql://mysql:3306/winga_db?...
```

Backend container inapokea:
- `SPRING_DATASOURCE_URL` — tayari imewekwa na compose kwa `mysql:3306`
- `DB_USERNAME` = `${MYSQL_USER:-winga}`
- `DB_PASSWORD` = `${MYSQL_PASSWORD}`

**Kuupdate database details kwenye Docker:** badilisha `.env` (MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD), kisha:
```bash
docker compose down
docker compose up -d
```

### C. Production (server / VPS)

- **Ili database iwe kwenye server tofauti:** weka `SPRING_DATASOURCE_URL` kwenye environment (au profile `prod`):
  ```bash
  export SPRING_DATASOURCE_URL="jdbc:mysql://db.example.com:3306/winga_db?useSSL=true&serverTimezone=Africa/Dar_es_Salaam"
  export DB_USERNAME=winga_prod
  export DB_PASSWORD=YourSecurePassword
  ```
- **Kwa Docker production:** tumia `.env` sawa na B, au secrets za Docker.

---

## 3. Muhtasari

| Mazingira | Faili / mahali | Vigezo vya kubadilisha |
|-----------|----------------|------------------------|
| **Dev (local)** | `application.yml` + env (DB_USERNAME, DB_PASSWORD) | URL inayotumika iko kwenye `spring.datasource.url` |
| **Docker** | `.env` karibu na `docker-compose.yml` | MYSQL_DATABASE, MYSQL_USER, MYSQL_PASSWORD, MYSQL_ROOT_PASSWORD |
| **Production** | Environment variables kwenye server / container | SPRING_DATASOURCE_URL, DB_USERNAME, DB_PASSWORD |

Baada ya kubadilisha, restart application (au Docker services) ili mabadiliko yatumike.
