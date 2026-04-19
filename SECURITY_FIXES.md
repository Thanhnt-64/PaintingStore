# 🔐 SECURITY TROUBLESHOOTING GUIDE - ManageStore

## ❌ Các lỗi đã tìm thấy trong hệ thống bảo mật

### 🚨 **NGHIÊM TRỌNG - Cần sửa ngay lập tức**

#### 1. **Database Configuration Conflict**
**Triệu chứng:** Ứng dụng không start hoặc database connection error
**Nguyên nhân:** File `application.properties` có cấu hình datasource bị duplicate
**✅ ĐÃ SỬA:** Cleaned up application.properties

#### 2. **Redis Service Not Running**
**Triệu chứng:** 
- Đăng ký không gửi được OTP
- Đăng nhập thành công nhưng logout không blacklist token
- Error logs: "Connection refused" hoặc "Redis template error"

**🔧 Giải pháp:**
```powershell
# Chạy script setup Redis (requires Administrator)
.\scripts\setup-redis-windows.ps1

# Hoặc cài đặt thủ công:
choco install redis-64 -y
Start-Service redis

# Kiểm tra Redis hoạt động:
redis-cli ping  # Phải trả về "PONG"
```

#### 3. **Email Service Not Configured**
**Triệu chứng:** OTP không được gửi đến email
**Nguyên nhân:** SMTP settings chưa được cấu hình

**🔧 Cách cấu hình Gmail SMTP:**
```properties
# Trong application.properties
spring.mail.username=your-gmail@gmail.com
spring.mail.password=your-app-password  # NOT your Gmail password!
```

**🔑 Tạo App Password cho Gmail:**
1. Vào Google Account Settings
2. Security → 2-Step Verification (bật nếu chưa có)
3. App passwords → Generate app password
4. Sử dụng password này trong config (16 ký tự)

### ⚠️ **TRUNG BÌNH - Nên sửa**

#### 4. **JWT Secret Too Weak**
**✅ ĐÃ SỬA:** Updated to stronger secret key

#### 5. **Error Handling Improvements**
**✅ ĐÃ SỬA:** Added graceful handling for Redis failures

#### 6. **Security Configuration Warnings**
**✅ ĐÃ SỬA:** Disabled JPA open-in-view and improved configurations

---

## 🧪 **TESTING CHECKLIST**

### 1. **Test Database Connection**
```bash
# Kiểm tra MySQL running
mysql -u root -p -e "SHOW DATABASES;"

# Tìm database painting_store
mysql -u root -p painting_store -e "SHOW TABLES;"
```

### 2. **Test Redis Connection**
```bash
redis-cli ping           # Should return PONG
redis-cli set test value # Test write
redis-cli get test       # Should return "value"
redis-cli del test       # Cleanup
```

### 3. **Test Application Start**
```bash
cd ManageStore
mvn clean compile       # Should compile without errors
mvn spring-boot:run     # Should start without Redis errors
```

### 4. **Test Authentication APIs**

**Đăng ký user:**
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "testuser", "email": "test@test.com", "password": "password123"}'
```

**Kiểm tra email (nếu SMTP đã config):**
- Check email inbox cho OTP code

**Confirm registration:**
```bash
curl -X POST http://localhost:8080/api/auth/confirm \
  -H "Content-Type: application/json" \
  -d '{"email": "test@test.com", "otp": "123456"}'
```

**Đăng nhập:**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail": "testuser", "password": "password123"}'
```

**Test protected endpoint:**
```bash
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

---

## 🛠️ **QUICK FIX COMMANDS**

### Start all required services:
```powershell
# Start MySQL (if using XAMPP)
# Start Redis
Start-Service redis

# Start Application
mvn spring-boot:run
```

### Check service status:
```powershell
# Check Redis
redis-cli ping

# Check MySQL
mysql -u root -p -e "SELECT 1;"

# Check Application
curl http://localhost:8080/actuator/health
```

### View logs for debugging:
```powershell
# Application logs (trong terminal chạy mvn spring-boot:run)
# Redis logs
Get-WinEvent -LogName Application | Where-Object {$_.ProviderName -eq "redis"}
```

---

## 🔒 **SECURITY BEST PRACTICES IMPLEMENTED**

✅ **Password Security:**
- BCrypt hashing with strong salt
- No plaintext password storage

✅ **JWT Security:**
- Strong secret key (256-bit)
- Token expiration (1 hour)
- Token blacklisting on logout

✅ **OTP Security:**
- 6-digit numeric OTP
- 5-minute expiration
- Rate limiting (5 OTP/hour per email)
- No OTP logging

✅ **API Security:**
- Public endpoints: `/api/auth/*`
- Protected endpoints require JWT
- CORS configuration
- Input validation

✅ **Error Handling:**
- Generic error messages (don't reveal system details)
- Graceful degradation when Redis fails
- Proper exception handling

---

## 📞 **SUPPORT**

Nếu vẫn gặp lỗi sau khi làm theo hướng dẫn:

1. **Check logs** trong terminal chạy `mvn spring-boot:run`
2. **Verify services:**
   - MySQL: `mysql -u root -p -e "SELECT 1;"`
   - Redis: `redis-cli ping`
3. **Check ports:**
   - App: http://localhost:8080
   - MySQL: localhost:3306
   - Redis: localhost:6379

**Common Issues:**
- Port 8080 đã được sử dụng → Đổi port trong application.properties
- Redis connection refused → Chạy `Start-Service redis`
- MySQL access denied → Kiểm tra username/password trong config
- Email không gửi → Kiểm tra Gmail app password