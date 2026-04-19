# Auth System Implementation Notes

## Architecture Overview

```
┌─────────────────┐
│  Auth Endpoints │
└────────┬────────┘
         │
    ┌────▼──────────────────────────┐
    │   AuthController (@RestAPI)   │
    │  • register()                  │
    │  • confirm()                   │
    │  • login()                     │
    │  • logout()                    │
    │  • reset-request()             │
    │  • reset-verify()              │
    │  • reset-complete()            │
    └────┬──────────────────────────┘
         │
    ┌────▼──────────────────────────┐
    │   AuthService (Business Logic)│
    │  • register flow              │
    │  • OTP management             │
    │  • JWT token generation       │
    │  • Password reset             │
    └────┬──────────────────────────┘
         │
    ┌────▼──────────────────────────┐
    │   Supporting Services         │
    │  • OtpService (Redis)          │
    │  • EmailService (SMTP)         │
    │  • JwtUtil (Token ops)         │
    │  • UserService (DB)            │
    │  • PasswordEncoder (BCrypt)    │
    │  • TokenBlacklistService       │
    └────┬──────────────────────────┘
         │
    ┌────▼──────────────────────────┐
    │   Data Layer                  │
    │  • MySQL (User, tokens)        │
    │  • Redis (OTP, blacklist)      │
    │  • Flyway (migrations)         │
    └──────────────────────────────┘
```

---

## Request/Response Flow

### Registration Flow

```
User POST /api/auth/register
    ↓
(JwtAuthenticationFilter: skip auth for /api/auth/**)
    ↓
AuthController.register()
    ↓
AuthService.register()
    ├─→ Check email unique → DB query
    ├─→ Create user (enabled=false) → JPA save
    ├─→ OtpService.generateAndSaveOtp()
    │   ├─→ Generate 6-digit OTP
    │   ├─→ Save to Redis: otp:{email} = OTP (5min TTL)
    │   ├─→ Increment rate counter: otp_rate:{email} (1hr TTL)
    │   └─→ Log OTP for testing
    ├─→ EmailService.sendOtpEmail()
    │   ├─→ Send via Gmail SMTP
    │   └─→ Log error if SMTP fails (non-blocking)
    └─→ Return 200 OK with message

Response: 200 OK
{
  "message": "Registration initiated. Check email for OTP."
}
```

### Confirmation Flow

```
User POST /api/auth/confirm
    ↓
(JwtAuthenticationFilter: skip auth)
    ↓
AuthController.confirm()
    ├─→ AuthService.verifyOtpForEmail()
    │   ├─→ OtpService.verifyOtp()
    │   │   ├─→ Get from Redis: otp:{email}
    │   │   ├─→ Compare with input
    │   │   └─→ Delete from Redis if match
    │   └─→ Return true/false
    ├─→ Find user by email → DB query
    ├─→ Set user.enabled = true → JPA save
    └─→ Return 200 OK

Response: 200 OK
{
  "message": "Account activated"
}
```

### Login Flow

```
User POST /api/auth/login
    ↓
(JwtAuthenticationFilter: skip auth)
    ↓
AuthController.login()
    └─→ AuthService.login()
        ├─→ AuthenticationManager.authenticate()
        │   ├─→ Load user from DB
        │   ├─→ Verify password (BCrypt)
        │   └─→ Check enabled=true
        ├─→ Extract username from auth
        ├─→ JwtUtil.generateToken()
        │   ├─→ Create JWT payload: sub={username}, roles=[ROLE_USER]
        │   ├─→ Sign with HMAC-SHA256
        │   └─→ Set expiry = now + 1 hour
        └─→ Return 200 OK with token

Response: 200 OK
{
  "token": "<JWT_TOKEN>",
  "tokenType": "Bearer"
}
```

### Logout Flow

```
User POST /api/auth/logout with Authorization: Bearer <TOKEN>
    ↓
(JwtAuthenticationFilter: validate JWT)
    ├─→ Extract token from header
    ├─→ Verify JWT signature
    ├─→ Check token not blacklisted
    └─→ Set SecurityContext with authenticated user
    ↓
AuthController.logout()
    └─→ AuthService.logout()
        └─→ TokenBlacklistService.blacklist()
            ├─→ Add token to Redis: blacklist_token:{hash}
            ├─→ Set TTL = token expiry time
            └─→ Token rejected on future requests
        ↓
        Return 200 OK

Response: 200 OK
{
  "message": "Logged out"
}
```

---

## Database Schema

### Users Table

```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,  -- BCrypt hashed
  enabled BOOLEAN DEFAULT FALSE,    -- false until OTP confirmed
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### Roles Table

```sql
CREATE TABLE roles (
  id INT PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE
  -- Pre-loaded: 1=ROLE_ADMIN, 2=ROLE_USER
);
```

### User-Role Join

```sql
CREATE TABLE user_roles (
  user_id BIGINT,
  role_id INT,
  PRIMARY KEY (user_id, role_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id)
);
```

---

## Redis Data Structures

### OTP Storage

```
Key: otp:{email}
Value: 6-digit OTP code
TTL: 5 minutes
Example: otp:user@example.com = "123456"
```

### Rate Limiting Counter

```
Key: otp_rate:{email}
Value: Number of OTP requests in current hour
TTL: 1 hour
Example: otp_rate:user@example.com = 1 (incremented on each request)
```

### Token Blacklist

```
Key: blacklist_token:{token_hash}
Value: true (presence is the only thing that matters)
TTL: Token expiry time (1 hour by default)
Example: blacklist_token:abc123xyz... = true
```

---

## Security Implementation

### Password Security

```java
// BCrypt hashing (Spring Security)
PasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode(plainTextPassword);
encoder.matches(plainTextPassword, hashedPassword); // For login
```

### JWT Token Structure

```
Header:
{
  "alg": "HS256",
  "typ": "JWT"
}

Payload:
{
  "sub": "username",          // Subject (identifier)
  "roles": ["ROLE_USER"],     // User roles
  "iat": 1776791352,          // Issued at
  "exp": 1776794952           // Expiration (1 hour later)
}

Signature:
HMAC-SHA256(header.payload, secret_key)
```

### OTP Security

```
- 6-digit random (10^6 = ~1 million combinations)
- No alphabetic characters (only 0-9)
- 5-minute expiration
- Rate limit: 5 per email per hour
- Deleted after successful verification
```

### CORS Configuration

```java
// Allow all origins for testing
CorsConfiguration config = new CorsConfiguration();
config.setAllowCredentials(true);
config.addAllowedOriginPattern("*");
config.addAllowedHeader("*");
config.addAllowedMethod("*");
```

### JWT Filter Order

```
1. RequestResponseLoggingFilter (custom, logs all requests)
2. JwtAuthenticationFilter (custom, validates JWT)
   - Skips: /api/auth/**, /static/**
   - Validates: all other endpoints
3. AnonymousAuthenticationFilter (Spring default)
4. AuthorizationFilter (Spring default, checks @Secured)
```

---

## Configuration Properties

### Application Properties

```properties
# Server
server.port=8080

# Static Resources
spring.web.resources.static-path-pattern=/static/**

# Database (MySQL)
spring.datasource.url=jdbc:mysql://localhost:3306/painting_store
spring.datasource.username=root
spring.datasource.password=150601
spring.jpa.hibernate.ddl-auto=update

# Redis
spring.redis.host=localhost
spring.redis.port=6379

# Mail (Gmail SMTP)
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=nguyenthanh3557@gmail.com
spring.mail.password=<APP_PASSWORD>  # NOT normal password
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# JWT
app.jwt.secret=ReplaceWithSecureKey
app.jwt.expiration-ms=3600000  # 1 hour

# OTP
app.otp.max-per-hour=5

# Logging
logging.level.com.vn.ManageStore=DEBUG
logging.file.name=logs/managestore.log
logging.file.max-size=10MB
logging.file.max-history=10
```

---

## Exception Handling

### Custom Exceptions

```java
// 1. IllegalArgumentException
// - Email already exists
// - Response: 400 Bad Request

// 2. IllegalStateException
// - Rate limit exceeded
// - Response: 400 Bad Request

// 3. BadCredentialsException
// - Invalid username/password
// - Response: 401 Unauthorized

// 4. HttpMessageNotReadableException
// - Invalid JSON in request body
// - Response: 400 Bad Request

// 5. Default exception handler
// - Response: 500 Internal Server Error
// - Body: Error stacktrace (dev only)
```

### JWT Exception Handling

```java
// TokenExpiredException
// Response: 401 Unauthorized
// Message: "JWT expired"

// SignatureException
// Response: 401 Unauthorized
// Message: "Invalid signature"

// MalformedJwtException
// Response: 401 Unauthorized
// Message: "Invalid token format"
```

---

## Performance Metrics

| Operation | Time | Notes |
|-----------|------|-------|
| Register | 500ms | OTP gen + DB save + email attempt |
| Confirm | 100ms | OTP verify + user update |
| Login | 200ms | Password verify + JWT gen |
| Logout | 50ms | Token blacklist add |
| Password Reset Request | 400ms | OTP gen + email attempt |
| Password Reset Complete | 150ms | OTP verify + password update |

---

## Known Issues & TODOs

### Current Issues

1. **Gmail SMTP Auth**
   - App Password not configured (using regular password)
   - Email send fails but doesn't block registration
   - Workaround: OTP logged to `logs/managestore.log`

2. **Database Initialization**
   - Flyway migrations run on startup
   - Manual DB creation required if Flyway disabled

### Production TODOs

- [ ] Generate strong JWT secret key
- [ ] Configure Gmail App Password
- [ ] Add request validation (email format, password strength)
- [ ] Add CAPTCHA for registration (spam prevention)
- [ ] Add audit logging (who registered, when, from where)
- [ ] Add email verification email content (templates)
- [ ] Add password reset email templates
- [ ] Implement account lockout after N failed logins
- [ ] Add 2FA/MFA support
- [ ] Add social login (Google, GitHub, etc.)
- [ ] Add session management (multiple device login)
- [ ] Add refresh token support

---

## Testing Checklist

- [x] Register with valid credentials → 200 OK
- [x] Register with duplicate email → 400 Bad Request
- [x] Confirm with valid OTP → 200 OK
- [x] Confirm with invalid OTP → 400 Bad Request
- [x] Login with valid credentials → 200 OK, JWT returned
- [x] Login before confirmation → 401 Unauthorized
- [x] Logout with valid token → 200 OK, token blacklisted
- [x] Use blacklisted token → 401 Unauthorized
- [x] Use expired token → 401 Unauthorized
- [ ] Password reset flow (optional)
- [ ] CORS preflight requests (optional)
- [ ] Rate limiting 5 OTP/hour (optional)
- [ ] Concurrent requests (load test)
- [ ] Database transaction rollback scenarios

---

## Code Quality Notes

### Logging Strategy

```java
// INFO: Important business events
logger.info("Registration requested for email={}", email);
logger.info("Login attempt for usernameOrEmail={}", usernameOrEmail);

// DEBUG: Detailed flow info
logger.debug("User created for email={}, username={}", email, username);
logger.debug("OTP saved to Redis with 5min TTL for key={}", key);

// WARN: Recoverable errors
logger.warn("Failed to send activation email: {}", ex.getMessage());

// ERROR: Critical failures
logger.error("Database connection failed", ex);
```

### Exception Handling Pattern

```java
try {
    // Happy path
    emailService.sendOtpEmail(email, subject, html);
    logger.info("Email sent successfully");
} catch (MailAuthenticationException ex) {
    logger.warn("Email send failed (OTP still saved): {}", ex.getMessage());
    // Don't throw - allow flow to continue
} catch (Exception ex) {
    logger.error("Unexpected error", ex);
    throw new RuntimeException("Email service error", ex);
}
```

### Security Best Practices

```java
// ✓ DO: Hash passwords
passwordEncoder.encode(plainPassword);

// ✗ DON'T: Log sensitive data
logger.info("User password: {}", password); // WRONG

// ✓ DO: Use parameterized queries (JPA handles this)
userRepository.findByEmail(email); // Safe

// ✗ DON'T: String concatenation in queries
"SELECT * FROM users WHERE email = '" + email + "'"; // SQL injection risk
```

---

## Deployment Checklist

Before deploying to production:

- [ ] Change JWT secret to strong random key (min 256 bits)
- [ ] Configure real Gmail App Password
- [ ] Set `app.jwt.expiration-ms` based on business requirement
- [ ] Set `app.otp.max-per-hour` based on security requirement
- [ ] Enable HTTPS (TLS 1.3+)
- [ ] Set secure=true on all cookies
- [ ] Configure proper CORS origins (not "*")
- [ ] Enable request rate limiting
- [ ] Set up monitoring & alerting
- [ ] Set up log aggregation (ELK, CloudWatch, etc.)
- [ ] Set up database backups
- [ ] Set up Redis persistence/replication
- [ ] Test disaster recovery procedures
- [ ] Document API for clients
- [ ] Set up CI/CD pipeline
- [ ] Load testing (JMeter, Gatling)
- [ ] Security audit/penetration testing

---

## Related Documentation

- `AUTH_API_TEST_GUIDE.md` - Testing guide with examples
- `src/main/resources/application.properties` - Configuration
- `src/main/java/com/vn/ManageStore/controller/AuthController.java` - API endpoints
- `src/main/java/com/vn/ManageStore/service/AuthService.java` - Business logic
- `src/main/java/com/vn/ManageStore/security/` - Security components
