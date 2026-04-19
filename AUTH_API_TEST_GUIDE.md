# Auth API Testing Guide

## Prerequisites

- **App Running**: `java -jar target/ManageStore-0.0.1-SNAPSHOT.jar` (Port 8080)
- **MySQL**: `painting_store` database created
- **Redis**: Running on localhost:6379
- **Email**: `nguyenthanh3557@gmail.com` (used in examples, adjust as needed)

---

## 1. Test: Register User

**Endpoint**: `POST /api/auth/register`

**Command**:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"testuser_001\",\"email\":\"nguyenthanh3557@gmail.com\",\"password\":\"Pass@123456\"}"
```

**Expected Response (200 OK)**:
```json
{
  "message": "Registration initiated. Check email for OTP."
}
```

**What Happens**:
- ✓ User created in DB (enabled=false initially)
- ✓ 6-digit OTP generated and saved to Redis with 5-minute TTL
- ✓ OTP logged in `logs/managestore.log` (format: `*** TEST OTP FOR EMAIL=...@gmail.com: XXXXXX ***`)
- ✗ Email send attempted (will fail if Gmail App Password not configured)
- ✓ Registration still succeeds even if email send fails

**Get OTP from Logs**:
```bash
tail -50 logs/managestore.log | grep "TEST OTP"
# Output example: *** TEST OTP FOR EMAIL=nguyenthanh3557@gmail.com: 149186 (expires in 5 minutes) ***
```

**Notes**:
- Each `username` must be unique
- Each `email` must be unique
- Password must contain: uppercase, lowercase, number, special char
- OTP expires in 5 minutes
- Max 5 OTP requests per email per hour

---

## 2. Test: Confirm Registration

**Endpoint**: `POST /api/auth/confirm`

**Command** (replace OTP with value from logs):
```bash
curl -X POST http://localhost:8080/api/auth/confirm \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"nguyenthanh3557@gmail.com\",\"otp\":\"149186\"}"
```

**Expected Response (200 OK)**:
```json
{
  "message": "Account activated"
}
```

**What Happens**:
- ✓ OTP verified against Redis
- ✓ User `enabled` flag set to `true` in DB
- ✓ OTP deleted from Redis after verification
- Account is now active and can login

**Error Cases**:
```bash
# Invalid OTP (wrong code)
# Response: 400 Bad Request
# Body: {"message": "Invalid OTP"}

# Email not found
# Response: 400 Bad Request
# Body: {"message": "User not found"}

# OTP expired (> 5 minutes)
# Response: 400 Bad Request
# Body: {"message": "Invalid OTP"}
```

---

## 3. Test: Login

**Endpoint**: `POST /api/auth/login`

**Command** (after confirming registration):
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrEmail\":\"nguyenthanh3557@gmail.com\",\"password\":\"Pass@123456\"}"
```

**Expected Response (200 OK)**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0X3JlYWxfZW1haWxfMSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3NzY3OTEzNTIsImV4cCI6MTc3Njc5NDk1Mn0.0HqjO--xOq0S-wM3eqJSlJfdWxA0kfZWbzBLDO5_iE4",
  "tokenType": "Bearer"
}
```

**Token Info**:
- Valid for **1 hour** from issuance
- Algorithm: HMAC-SHA256
- Can use either `username` or `email` to login
- Token format: `Bearer <token>`

**Error Cases**:
```bash
# Account not confirmed (enabled=false)
# Response: 401 Unauthorized
# Body: Bad credentials

# Wrong password
# Response: 401 Unauthorized
# Body: Bad credentials

# User not found
# Response: 401 Unauthorized
# Body: Bad credentials
```

---

## 4. Test: Logout

**Endpoint**: `POST /api/auth/logout`

**Command** (use token from login response):
```bash
# Save token from login
TOKEN="eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0X3JlYWxfZW1haWxfMSIsInJvbGVzIjpbIlJPTEVfVVNFUiJdLCJpYXQiOjE3NzY3OTEzNTIsImV4cCI6MTc3Njc5NDk1Mn0.0HqjO--xOq0S-wM3eqJSlJfdWxA0kfZWbzBLDO5_iE4"

# Logout
curl -X POST http://localhost:8080/api/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN"
```

**Expected Response (200 OK)**:
```json
{
  "message": "Logged out"
}
```

**What Happens**:
- ✓ Token added to Redis blacklist
- ✓ Token expires from Redis after 1 hour (matches JWT expiry)
- Token cannot be used for authenticated requests after logout

**Error Cases**:
```bash
# No Authorization header
# Response: 200 OK (still returns success)
# Body: {"message": "Logged out"}

# Invalid token format
# Response: 401 Unauthorized
# Body: Full auth exception page
```

---

## 5. Test: Password Reset Flow

### 5.1 Request Password Reset

**Endpoint**: `POST /api/auth/reset-request`

**Command**:
```bash
curl -X POST http://localhost:8080/api/auth/reset-request \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"nguyenthanh3557@gmail.com\"}"
```

**Expected Response (200 OK)**:
```json
{
  "message": "If the email exists, an OTP has been sent"
}
```

**What Happens**:
- ✓ OTP generated (different from registration OTP)
- ✓ OTP saved to Redis with 5-minute TTL
- ✓ OTP logged in `logs/managestore.log`
- ✗ Email send attempted (will fail if Gmail not configured)
- ✓ Request succeeds even if email fails

**Get Reset OTP**:
```bash
tail -50 logs/managestore.log | grep "TEST OTP"
```

### 5.2 Verify Reset OTP

**Endpoint**: `POST /api/auth/reset-verify`

**Command**:
```bash
curl -X POST http://localhost:8080/api/auth/reset-verify \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"nguyenthanh3557@gmail.com\",\"otp\":\"123456\"}"
```

**Expected Response (200 OK)**:
```json
{
  "message": "OTP valid"
}
```

**What Happens**:
- ✓ OTP verified against Redis
- ✓ OTP is NOT deleted (needed for complete reset)

### 5.3 Complete Password Reset

**Endpoint**: `POST /api/auth/reset-complete`

**Command**:
```bash
curl -X POST http://localhost:8080/api/auth/reset-complete \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"nguyenthanh3557@gmail.com\",\"otp\":\"123456\",\"newPassword\":\"NewPass@654321\"}"
```

**Expected Response (200 OK)**:
```json
{
  "message": "Password updated"
}
```

**What Happens**:
- ✓ OTP verified
- ✓ User password updated in DB (BCrypt hashed)
- ✓ OTP deleted from Redis
- Can now login with new password

---

## Complete Test Sequence (Copy-Paste Ready)

```bash
#!/bin/bash

# Variables
EMAIL="nguyenthanh3557@gmail.com"
USERNAME="testuser_$(date +%s)"
PASSWORD="Pass@123456"
BASE_URL="http://localhost:8080"

echo "=== 1. REGISTER USER ==="
REGISTER_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/register \
  -H "Content-Type: application/json" \
  -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "$REGISTER_RESPONSE"

echo ""
echo "=== Get OTP from logs ==="
OTP=$(grep "TEST OTP FOR EMAIL=$EMAIL" logs/managestore.log | tail -1 | grep -oE '[0-9]{6}' | head -1)
echo "OTP: $OTP"

echo ""
echo "=== 2. CONFIRM REGISTRATION ==="
CONFIRM_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/confirm \
  -H "Content-Type: application/json" \
  -d "{\"email\":\"$EMAIL\",\"otp\":\"$OTP\"}")
echo "$CONFIRM_RESPONSE"

echo ""
echo "=== 3. LOGIN ==="
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/login \
  -H "Content-Type: application/json" \
  -d "{\"usernameOrEmail\":\"$EMAIL\",\"password\":\"$PASSWORD\"}")
echo "$LOGIN_RESPONSE"

# Extract token (requires jq)
TOKEN=$(echo "$LOGIN_RESPONSE" | grep -oE '"token":"[^"]*' | cut -d'"' -f4)
echo "Token: $TOKEN"

echo ""
echo "=== 4. LOGOUT ==="
LOGOUT_RESPONSE=$(curl -s -X POST $BASE_URL/api/auth/logout \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN")
echo "$LOGOUT_RESPONSE"

echo ""
echo "=== Test Complete ==="
```

---

## Database Cleanup

### Reset All Data
```bash
# MySQL
mysql -u root -p150601 -e "DROP DATABASE painting_store; CREATE DATABASE painting_store;"
```

### Redis Cleanup
```bash
# Clear all Redis keys (OTP, blacklist, etc.)
redis-cli FLUSHDB
```

---

## Logs Location

**File**: `logs/managestore.log`

**Key Log Patterns**:
```
# Registration
"Registration requested for email="
"User created for email="
"*** TEST OTP FOR EMAIL=... ***"

# Login
"Login attempt for usernameOrEmail="
"Set SecurityContext for user="

# Logout
"Token blacklisted"

# Errors
"Failed to send activation email"
"Duplicate entry"
"Invalid OTP"
```

**View Live Logs**:
```bash
# Last 50 lines
tail -50 logs/managestore.log

# Filter by action
grep "TEST OTP" logs/managestore.log
grep "Login attempt" logs/managestore.log
grep "ERROR" logs/managestore.log
```

---

## Troubleshooting

### Issue: Email Authentication Failed

**Symptom**: Registration returns 200 but OTP not received

**Cause**: Gmail App Password incorrect or not configured

**Fix**:
1. Generate App Password from Google Account (https://myaccount.google.com/apppasswords)
2. Update `application.properties`:
   ```properties
   spring.mail.password=<16-char-app-password>
   ```
3. Rebuild: `mvn clean install -DskipTests`
4. Restart app

**Workaround**: OTP is logged to `logs/managestore.log`, use that for testing

### Issue: OTP Expired

**Symptom**: Confirm returns "Invalid OTP"

**Cause**: 5-minute TTL exceeded

**Fix**: Generate new OTP by calling register endpoint again

### Issue: Duplicate Email/Username

**Symptom**: Register returns 400 "Email/Username already in use"

**Fix**: Use unique email/username for each test

**Or**: Reset database:
```bash
mysql -u root -p150601 -e "DROP DATABASE painting_store; CREATE DATABASE painting_store;"
```

### Issue: Token Blacklist Not Working

**Symptom**: Logout succeeds but token still accepted

**Possible Cause**: Redis not running or connection failed

**Check Redis**:
```bash
redis-cli ping
# Should return: PONG
```

---

## Performance Notes

- **Register**: ~500ms (includes OTP generation, Redis save, email attempt)
- **Confirm**: ~100ms (OTP verification, user update)
- **Login**: ~200ms (password verification, JWT generation)
- **Logout**: ~50ms (token blacklist add)

---

## Security Notes

✓ Passwords hashed with BCrypt  
✓ JWT signed with HMAC-SHA256  
✓ OTP 6-digit random (10^6 possibilities)  
✓ Token blacklist on logout  
✓ Rate limiting: 5 OTP per email per hour  
✓ CORS enabled for testing  
✓ CSRF disabled (stateless)  

⚠️ **Production TODO**:
- Change `app.jwt.secret` to strong random key
- Set up proper Gmail App Password
- Enable CSRF if frontend is same-origin
- Add request validation (currently lenient)
- Add audit logging to database
