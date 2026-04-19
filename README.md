# ManageStore - Authentication Module

This module provides REST APIs for user authentication: register (with email OTP confirmation), login (JWT), logout (JWT blacklist), password reset via email OTP. OTP and token blacklist use Redis. Emails are sent using Spring JavaMailSender.

Configuration (edit `src/main/resources/application.properties`):
- `spring.mail.*` - SMTP settings
- `spring.redis.*` - Redis settings
- `app.jwt.secret` - JWT secret (must be long enough for HS256)

Available endpoints (JSON requests/responses):
- POST /api/auth/register { username, email, password } -> starts registration and sends OTP to email
- POST /api/auth/confirm { email, otp } -> confirm registration
- POST /api/auth/login { usernameOrEmail, password } -> returns { token }
- POST /api/auth/logout Authorization: Bearer <token> -> blacklist token
- POST /api/auth/reset-request { email } -> sends reset OTP
- POST /api/auth/reset-verify { email, otp } -> verify OTP
- POST /api/auth/reset-complete { email, otp, newPassword } -> set new password

Notes & security:
- Passwords are hashed with BCrypt before persisting.
- OTP values are stored in Redis for 5 minutes and not logged.
- Tokens are JWT and blacklisted on logout in Redis until their expiry.
- Rate limit for OTP requests per email is enforced via Redis counter.

How to run:
1. Start Redis locally (or set `spring.redis.*` to your Redis).
2. Configure SMTP in `application.properties`.
3. Build and run with Maven:

```powershell
mvn -DskipTests spring-boot:run
```
