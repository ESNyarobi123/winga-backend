# Winga — Docker Deployment (VPS)

Mwongozo wa ku-deploy **Winga Backend**, **Winga UI (Frontend)**, **Admin Dashboard**, **MySQL** na **phpMyAdmin** kwenye VPS yako kwa Docker.

---

## Domains (mfano wako)

| Huduma        | URL |
|---------------|-----|
| Winga UI Frontend | https://winga.sokokuuonline.co.tz |
| phpMyAdmin | https://swinga.sokokuuonline.co.tz/phpmyadmin |
| Backend API | https://api.sokokuuonline.co.tz |
| Admin Dashboard | https://admin.sokokuuonline.co.tz |

---

## 1. Vitaratibu (VPS)

- **Docker** na **Docker Compose** vimeinstall.
- **Git** iko kwenye server.
- Domains zimeelekeza kwenye IP ya VPS (A record):
  - `winga.sokokuuonline.co.tz` → IP ya VPS
  - `swinga.sokokuuonline.co.tz` → IP ya VPS
  - `api.sokokuuonline.co.tz` → IP ya VPS
  - `admin.sokokuuonline.co.tz` → IP ya VPS

---

## 2. Muundo wa Folders

Weka miradi kwa muundo huu (kwenye VPS):

```text
/opt/winga/                    # au path yoyote unayopenda
├── docker-compose.yml         # kutoka docs/deploy/
├── .env                       # kutoka docs/deploy/.env.example (weka thamani)
├── backend/                   # clone ya winga-backend
│   ├── Dockerfile
│   ├── pom.xml
│   └── src/
└── frontend/                  # clone ya Winga-ui
    ├── Dockerfile
    ├── package.json
    ├── src/
    └── admin-dashboard/
        ├── Dockerfile
        └── ...
```

---

## 3. Hatua kwa Hatua

### 3.1 Tengeneza folder na clone repos

```bash
sudo mkdir -p /opt/winga
cd /opt/winga

# Clone Backend
git clone https://github.com/ESNyarobi123/winga-backend.git backend

# Clone Frontend (Winga UI)
git clone https://github.com/ESNyarobi123/Winga-ui.git frontend
```

### 3.2 Copy docker-compose na .env

```bash
cd /opt/winga

cp backend/docs/deploy/docker-compose.yml .
cp backend/docs/deploy/.env.example .env

# Edit .env — weka passwords na JWT secret
nano .env
```

**Obligatory kwenye `.env`:**
- `MYSQL_ROOT_PASSWORD` — password ya MySQL root
- `MYSQL_PASSWORD` — password ya user `winga` (database ya app)
- `JWT_SECRET` — string ndefu ya siri (angalau 32 characters)

Mfano:

```env
MYSQL_ROOT_PASSWORD=StrongRootPass123!
MYSQL_DATABASE=winga_db
MYSQL_USER=winga
MYSQL_PASSWORD=WingaDbPass456!
JWT_SECRET=your-super-secret-jwt-key-at-least-32-chars-long
APP_CORS_ALLOWED_ORIGINS=https://winga.sokokuuonline.co.tz,https://admin.sokokuuonline.co.tz
REFERRAL_BASE_URL=https://winga.sokokuuonline.co.tz
NEXT_PUBLIC_API_URL=https://api.sokokuuonline.co.tz
```

### 3.3 Build na Run

```bash
cd /opt/winga
docker compose up -d --build
```

Hii ita:
- Build na kuanza **MySQL** (port 3306)
- **phpMyAdmin** (port 8081)
- **Backend** (port 8080)
- **Frontend** (port 3000)
- **Admin** (port 3001)

### 3.4 Kuangalia

```bash
docker compose ps
docker compose logs -f backend   # angalia logs ya backend
```

- Backend: `http://IP_YAKO:8080/swagger-ui.html`
- Frontend: `http://IP_YAKO:3000`
- Admin: `http://IP_YAKO:3001`
- phpMyAdmin: `http://IP_YAKO:8081`

Database **winga_db** inatengenezwa na MySQL; Backend ina **Hibernate `ddl-auto: update`** kwa hivyo tables zitaundwa/update kwa kwanza kuanza.

---

## 4. Nginx (Reverse Proxy) kwenye VPS

Ili kutumia domains na HTTPS, weka **Nginx** kwenye VPS (nje ya Docker) kama reverse proxy.

### 4.1 Install Nginx na Certbot (SSL)

```bash
sudo apt update
sudo apt install -y nginx certbot python3-certbot-nginx
```

### 4.2 Config ya Nginx

Weka config kwa kila domain. Mfano:

**Frontend (winga.sokokuuonline.co.tz)**

```nginx
# /etc/nginx/sites-available/winga.sokokuuonline
server {
    listen 80;
    server_name winga.sokokuuonline.co.tz www.winga.sokokuuonline.co.tz;

    location / {
        proxy_pass http://127.0.0.1:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**phpMyAdmin (swinga.sokokuuonline.co.tz/phpmyadmin)**

```nginx
# /etc/nginx/sites-available/swinga.sokokuuonline
server {
    listen 80;
    server_name swinga.sokokuuonline.co.tz www.swinga.sokokuuonline.co.tz;

    location /phpmyadmin/ {
        proxy_pass http://127.0.0.1:8081/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    location = /phpmyadmin {
        return 302 /phpmyadmin/;
    }
}
```

**Backend API (api.sokokuuonline.co.tz)**

```nginx
# /etc/nginx/sites-available/api.sokokuuonline
server {
    listen 80;
    server_name api.sokokuuonline.co.tz;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

**Admin (admin.sokokuuonline.co.tz)**

- **Muhimu:** Ikiwa admin inatumia relative URL `/api/...` (na hivyo inapost kwa admin.sokokuuonline.co.tz), ongeza **proxy ya `/api`** kwa backend. Vinginevyo utapata **405 Not Allowed** na "Backend returned invalid response (HTML)".

```nginx
# /etc/nginx/sites-available/admin.sokokuuonline
server {
    listen 80;
    server_name admin.sokokuuonline.co.tz;

    # Proxy API calls to backend (fixes 405 when admin calls /api/auth/login on same host)
    location /api/ {
        proxy_pass http://127.0.0.1:8080;
        proxy_http_version 1.1;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location / {
        proxy_pass http://127.0.0.1:3001;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

### 4.3 Kuwezesha na SSL

```bash
sudo ln -s /etc/nginx/sites-available/winga.sokokuuonline /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/swinga.sokokuuonline /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/api.sokokuuonline /etc/nginx/sites-enabled/
sudo ln -s /etc/nginx/sites-available/admin.sokokuuonline /etc/nginx/sites-enabled/
sudo nginx -t
sudo systemctl reload nginx
sudo certbot --nginx -d winga.sokokuuonline.co.tz -d swinga.sokokuuonline.co.tz -d api.sokokuuonline.co.tz -d admin.sokokuuonline.co.tz
```

Certbot ataongeza HTTPS kwa config ya Nginx.

---

## 5. Mabadiliko ya Baadaye

- **Code:** `git pull` ndani ya `backend/` na `frontend/`, kisha:
  ```bash
  docker compose up -d --build
  ```
- **Database:** Data iko kwenye volume `mysql_data`. Backup:
  ```bash
  docker compose exec mysql mysqldump -u root -p winga_db > backup.sql
  ```
- **Logs:** `docker compose logs -f backend` (au `frontend`, `admin`, `mysql`).

---

## 6. GitHub URLs (kumbukumbu)

- Backend: https://github.com/ESNyarobi123/winga-backend.git  
- Frontend: https://github.com/ESNyarobi123/Winga-ui.git  

---

## 7. Matatizo ya Kawaida

| Tatizo | Suluhu |
|--------|--------|
| Backend haijui MySQL | Subiri MySQL iwe “healthy”: `docker compose ps`. Backend inatumia `depends_on: mysql (healthy)`. |
| CORS errors kwenye browser | Hakikisha `APP_CORS_ALLOWED_ORIGINS` kwenye `.env` ina domains za frontend na admin (comma-separated). |
| Frontend haijui API | Hakikisha `NEXT_PUBLIC_API_URL=https://api.sokokuuonline.co.tz` wakati wa build (tayari iko kwenye `.env` na compose). |
| Admin haijui API | Build ya admin inatumia `VITE_API_URL`; compose inapita `NEXT_PUBLIC_API_URL`. Rebuild: `docker compose build admin --no-cache`. |
| Admin: 405 Not Allowed / "Backend returned invalid response (HTML)" | Request inakwenda kwa admin.sokokuuonline.co.tz/api/... badala ya backend. **Suluhu 1:** Rebuild admin na `VITE_API_URL=https://api.sokokuuonline.co.tz` (Vite inaembed env wakati wa build). **Suluhu 2:** Ongeza proxy ya `/api/` kwenye Nginx ya admin (tazama config ya admin hapa juu) ili /api/* ipitie kwa backend (127.0.0.1:8080). |
| **Admin login: 403 (empty body)** | **Suluhu 1:** Rebuild na redeploy backend ili `POST /api/auth/admin/login` iwe kwenye security whitelist. **Suluhu 2:** Ikiwa Nginx iko mbele ya API, hakikisha haizuii POST kwa `/api/auth/admin/login` (usitumie rule inayoblock path hii). **Suluhu 3:** CORS — hakikisha origin ya admin (e.g. `http://localhost:5174` au `https://admin.sokokuuonline.co.tz`) iko kwenye `APP_CORS_ALLOWED_ORIGINS` / `app.cors.allowed-origins-extra`. |

Ikiwa umefuata hatua hapa, backend, frontend, admin, MySQL na phpMyAdmin zinapaswa kufanya kazi na domains zako (winga.sokokuuonline.co.tz, swinga.sokokuuonline.co.tz/phpmyadmin, api.sokokuuonline.co.tz, admin.sokokuuonline.co.tz).
