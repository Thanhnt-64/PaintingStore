# Production HTTPS Setup: Nginx + Let's Encrypt + Spring Boot

Kiến trúc production tiêu chuẩn cho ManageStore API:
```
[Client] ──(HTTPS)── [Nginx Reverse Proxy] ──(HTTP)── [Spring Boot Tomcat :8080]
                      (SSL/TLS termination)
```

## Thành phần chính

### 1. **Tomcat (Embedded trong Spring Boot)**
- Chạy trên `localhost:8080` (HTTP nội bộ)
- Không cần SSL config vì Nginx sẽ xử lý
- Application code không cần thay đổi

### 2. **Nginx (Reverse Proxy)**
- Lắng nghe port `443` (HTTPS public)
- Chuyển tiếp request tới Tomcat `127.0.0.1:8080`
- Xử lý SSL/TLS encryption
- Caching, compression, load balancing

### 3. **Let's Encrypt (SSL Certificate)**
- Cấp SSL certificate **miễn phí**
- Renewal tự động qua `certbot`
- Certificate lưu tại: `/etc/letsencrypt/live/yourdomain.com/`

---

## Bước 1: Chuẩn bị Server (Ubuntu/Debian)

```bash
# Cập nhật package manager
sudo apt update && sudo apt upgrade -y

# Cài Nginx
sudo apt install nginx -y

# Cài Certbot (Let's Encrypt client)
sudo apt install certbot python3-certbot-nginx -y

# Cài Java 21 (nếu chưa có)
sudo apt install openjdk-21-jdk -y

# Xác minh cài đặt
java -version
nginx -v
certbot --version
```

---

## Bước 2: Cấp SSL Certificate từ Let's Encrypt

```bash
# Dừng Nginx tạm thời (để Certbot có thể xác minh)
sudo systemctl stop nginx

# Cấp certificate (thay yourdomain.com thành tên domain của bạn)
sudo certbot certonly --standalone \
  -d yourdomain.com \
  -d www.yourdomain.com \
  --agree-tos \
  --email admin@yourdomain.com \
  --non-interactive

# Kết quả certificate sẽ lưu tại:
# /etc/letsencrypt/live/yourdomain.com/fullchain.pem (public cert)
# /etc/letsencrypt/live/yourdomain.com/privkey.pem (private key)

# Khởi động lại Nginx
sudo systemctl start nginx
```

---

## Bước 3: Cấu hình Nginx Reverse Proxy

Tạo file: `/etc/nginx/sites-available/managestore`

```nginx
# HTTP redirect sang HTTPS
server {
    listen 80;
    listen [::]:80;
    server_name yourdomain.com www.yourdomain.com;
    
    # Redirect tất cả HTTP sang HTTPS
    return 301 https://$server_name$request_uri;
}

# HTTPS server
server {
    listen 443 ssl http2;
    listen [::]:443 ssl http2;
    server_name yourdomain.com www.yourdomain.com;

    # SSL Certificates từ Let's Encrypt
    ssl_certificate /etc/letsencrypt/live/yourdomain.com/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/yourdomain.com/privkey.pem;

    # SSL best practices
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 10m;

    # Security headers
    add_header Strict-Transport-Security "max-age=31536000; includeSubDomains" always;
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "SAMEORIGIN" always;
    add_header X-XSS-Protection "1; mode=block" always;

    # Reverse proxy to Spring Boot Tomcat
    location / {
        proxy_pass http://127.0.0.1:8080;
        
        # Preserve original request headers
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_set_header X-Forwarded-Host $server_name;
        
        # Timeouts
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }

    # Gzip compression
    gzip on;
    gzip_types text/plain text/css application/json application/javascript;
    gzip_min_length 1000;
}
```

**Kích hoạt cấu hình:**

```bash
# Kiểm tra syntax
sudo nginx -t

# Tạo symlink
sudo ln -s /etc/nginx/sites-available/managestore /etc/nginx/sites-enabled/

# Khởi động Nginx
sudo systemctl restart nginx

# Xác minh status
sudo systemctl status nginx
```

---

## Bước 4: Spring Boot Application Configuration

**File: `application-prod.properties`**

```properties
# Server
server.port=8080
server.address=127.0.0.1

# Database (Production)
spring.datasource.url=jdbc:mysql://db-host:3306/managestore_prod
spring.datasource.username=app_user
spring.datasource.password=SecurePassword123!
spring.jpa.hibernate.ddl-auto=validate

# Redis (Production)
spring.redis.host=redis-host
spring.redis.port=6379
spring.redis.password=RedisPassword123!

# Mail (SMTP production)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=noreply@yourdomain.com
spring.mail.password=app-specific-password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# JWT
app.jwt.secret=VeryLongSecureRandomKeyForProductionChangeMe!
app.jwt.expiration-ms=3600000

# Logging (Production: reduced verbosity)
logging.level.root=WARN
logging.level.com.vn.ManageStore=INFO
```

**Build & Deploy:**

```bash
# Build JAR
mvn clean package -DskipTests -Pprod

# Copy JAR to server
scp target/ManageStore-0.0.1-SNAPSHOT.jar user@yourdomain.com:/opt/managestore/

# SSH to server
ssh user@yourdomain.com

# Chạy app (nêu background)
cd /opt/managestore
java -Dspring.profiles.active=prod -jar ManageStore-0.0.1-SNAPSHOT.jar &

# Hoặc dùng systemd service (xem bước sau)
```

---

## Bước 5: Systemd Service (Tự động restart)

**File: `/etc/systemd/system/managestore.service`**

```ini
[Unit]
Description=ManageStore Spring Boot Application
After=network.target

[Service]
Type=simple
User=appuser
WorkingDirectory=/opt/managestore
Environment="JAVA_OPTS=-Dspring.profiles.active=prod"
ExecStart=/usr/bin/java -jar /opt/managestore/ManageStore-0.0.1-SNAPSHOT.jar
Restart=on-failure
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Kích hoạt service:**

```bash
sudo systemctl daemon-reload
sudo systemctl enable managestore
sudo systemctl start managestore
sudo systemctl status managestore

# View logs
journalctl -u managestore -f
```

---

## Bước 6: SSL Certificate Auto-Renewal

Let's Encrypt certificate chỉ có hiệu lực 90 ngày. Certbot tự động renew mỗi ngày qua cron:

```bash
# Kiểm tra renewal configuration
sudo systemctl list-timers snap.certbot.renew.timer

# Hoặc renew manual
sudo certbot renew --dry-run  # Test
sudo certbot renew            # Actual renewal

# Nếu renewal thành công, Nginx sẽ auto reload mới certificate
```

---

## Bước 7: Firewall Rules (UFW)

```bash
# Enable firewall
sudo ufw enable

# Allow SSH
sudo ufw allow 22/tcp

# Allow HTTP & HTTPS
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp

# Deny internal Tomcat port (không allow từ ngoài)
sudo ufw default deny incoming
sudo ufw default allow outgoing

# Check status
sudo ufw status verbose
```

---

## Bước 8: Testing & Monitoring

### Kiểm tra HTTPS hoạt động:

```bash
# Test từ máy local
curl -I https://yourdomain.com/api/auth/reset-request

# Kết quả mong đợi: HTTP/1.1 200 OK, no certificate warnings
```

### Kiểm tra SSL Rating:

Truy cập: https://www.ssllabs.com/ssltest/analyze.html?d=yourdomain.com

Mục tiêu: **A+ rating**

### Monitoring Logs:

```bash
# Nginx access/error logs
tail -f /var/log/nginx/access.log
tail -f /var/log/nginx/error.log

# Spring Boot logs
journalctl -u managestore -f

# Certificate renewal logs
grep certbot /var/log/syslog
```

---

## Diagram Hoàn chỉnh

```
┌─────────────────────────────────────────────────────────────┐
│                        Internet (HTTPS)                      │
│                      yourdomain.com:443                      │
└────────────────────────────┬────────────────────────────────┘
                             │
                ┌────────────▼──────────────┐
                │   Nginx Reverse Proxy     │
                │  ☑ SSL/TLS Termination    │
                │  ☑ HTTP→HTTPS redirect    │
                │  ☑ Security Headers       │
                │  ☑ Gzip Compression      │
                │  ☑ Load Balancing         │
                └────────────┬───────────────┘
                             │
            ┌────────────────▼───────────────────┐
            │   Spring Boot App (Internal HTTP)   │
            │        Tomcat :8080               │
            │    ☑ JWT Authentication          │
            │    ☑ OTP + Email                 │
            │    ☑ DB + Redis Integration      │
            └──────────────────────────────────┘
```

---

## Troubleshooting

### Certificate renewal fail:

```bash
sudo certbot renew --force-renewal -v

# Nếu lỗi, kiểm tra:
sudo certbot certificates
```

### Nginx not forwarding requests:

```bash
# Kiểm tra Nginx config
sudo nginx -t

# Kiểm tra Spring Boot chạy
lsof -i :8080

# Kiểm trace request
curl -v https://yourdomain.com/api/auth/login
```

### High latency qua proxy:

- Tăng buffer size Nginx: `proxy_buffer_size 128k`
- Kiểm tra network latency: `ping -c 10 yourdomain.com`

---

## Checklist Production

- ✅ Nginx cài & cấu hình reverse proxy
- ✅ Let's Encrypt certificate cấp thành công
- ✅ SSL test: A+ rating (ssllabs.com)
- ✅ Firewall rules: chỉ allow 80, 443, 22
- ✅ Spring Boot chạy trên 127.0.0.1:8080 (không public)
- ✅ Systemd service enable auto-restart
- ✅ Certificate renewal schedule hoạt động
- ✅ HSTS header enable (Strict-Transport-Security)
- ✅ Logs monitoring setup
- ✅ Database backup plan

**Tất cả API giờ đã HTTPS bảo mật! 🔒**
