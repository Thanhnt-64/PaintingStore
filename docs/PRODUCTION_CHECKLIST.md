# HTTPS Production Deployment Checklist

## Pre-Deployment (Local Development)
- [ ] All tests passing: `mvn test`
- [ ] No security warnings in logs
- [ ] Application builds successfully: `mvn clean package -DskipTests -Pprod`
- [ ] Endpoints tested locally (HTTP)
- [ ] Database migrations verified
- [ ] Redis connections tested

## Server Setup (Ubuntu/Debian)
- [ ] Server updated: `sudo apt update && sudo apt upgrade`
- [ ] Java 21 installed: `java -version` outputs 21.x.x
- [ ] Nginx installed: `nginx -v` outputs 1.18+ or 1.20+
- [ ] Certbot installed: `certbot --version`
- [ ] Firewall enabled: `sudo ufw status` shows active
- [ ] SSH key configured (no password login)

## Domain & DNS
- [ ] Domain registered and owned
- [ ] DNS A record points to server IP: `nslookup yourdomain.com`
- [ ] DNS TTL lowered (during setup): 300 seconds or less
- [ ] Both `yourdomain.com` and `www.yourdomain.com` resolve

## SSL Certificate (Let's Encrypt)
- [ ] Certificate obtained successfully: `sudo certbot certificates`
- [ ] Certificate files exist:
  - `/etc/letsencrypt/live/yourdomain.com/fullchain.pem`
  - `/etc/letsencrypt/live/yourdomain.com/privkey.pem`
- [ ] Certificate valid for at least 30 days
- [ ] Auto-renewal test passed: `sudo certbot renew --dry-run`

## Nginx Configuration
- [ ] Nginx config file valid: `sudo nginx -t` shows "successful"
- [ ] HTTP → HTTPS redirect working:
  ```bash
  curl -I http://yourdomain.com
  # Should return 301 Moved Permanently
  ```
- [ ] HTTPS endpoint responding:
  ```bash
  curl -I https://yourdomain.com
  # Should return 200 or 401 (expected for auth endpoints)
  ```
- [ ] SSL certificate properly loaded:
  ```bash
  echo | openssl s_client -servername yourdomain.com -connect yourdomain.com:443 2>/dev/null | grep "Verify return code"
  # Should show "Verify return code: 0 (ok)"
  ```

## Spring Boot Application
- [ ] Application profile set to `prod`: check `application-prod.properties`
- [ ] Database credentials configured and tested
- [ ] Redis connection details configured and tested
- [ ] SMTP email credentials configured and tested
- [ ] JWT secret is strong (>32 characters)
- [ ] Logging level set to WARN (reduce verbosity)
- [ ] Application runs successfully on localhost:8080
- [ ] Health endpoint accessible: `http://127.0.0.1:8080/actuator/health`

## Systemd Service
- [ ] Service file created: `/etc/systemd/system/managestore.service`
- [ ] Service enabled: `sudo systemctl enable managestore`
- [ ] Service starts successfully: `sudo systemctl start managestore`
- [ ] Service restarts on failure
- [ ] Logs visible: `sudo journalctl -u managestore`

## Firewall & Security
- [ ] Firewall enabled: `sudo ufw enable`
- [ ] SSH allowed: `sudo ufw status numbered` shows 22/tcp ALLOW
- [ ] HTTP allowed (80): for Let's Encrypt renewal
- [ ] HTTPS allowed (443): for client connections
- [ ] Tomcat port (8080) NOT exposed publicly
- [ ] Fail2ban installed (optional): `sudo systemctl status fail2ban`

## Application Testing
- [ ] Register endpoint works:
  ```bash
  curl -X POST https://yourdomain.com/api/auth/register \
    -H "Content-Type: application/json" \
    -d '{"username":"test","email":"test@example.com","password":"Pass123!"}'
  ```
- [ ] Login endpoint works:
  ```bash
  curl -X POST https://yourdomain.com/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"usernameOrEmail":"test","password":"Pass123!"}'
  ```
- [ ] JWT token received and valid
- [ ] Protected endpoints require Bearer token
- [ ] Logout endpoint blacklists token
- [ ] Email sending works (check spam folder)
- [ ] OTP functionality tested

## Monitoring & Logging
- [ ] Application logs visible: `sudo journalctl -u managestore -f`
- [ ] Nginx access logs: `tail -f /var/log/nginx/managestore-access.log`
- [ ] Nginx error logs: `tail -f /var/log/nginx/managestore-error.log`
- [ ] Database connection pool monitored
- [ ] Redis connection status checked
- [ ] Disk space adequate: `df -h` shows >20% free

## SSL/TLS Security
- [ ] SSL test A+ rating: https://www.ssllabs.com/ssltest/analyze.html
- [ ] HSTS header present: `curl -I https://yourdomain.com | grep Strict`
- [ ] TLS 1.2+ only (no SSL 3.0, TLS 1.0, 1.1)
- [ ] Strong ciphers configured
- [ ] Certificate chain valid
- [ ] OCSP stapling enabled

## Performance & Optimization
- [ ] Gzip compression enabled on Nginx
- [ ] Connection pooling configured (DB, Redis)
- [ ] Nginx buffer sizes tuned
- [ ] Spring Boot memory allocation (Xms, Xmx) appropriate
- [ ] Response times acceptable (<200ms p95)

## Backup & Recovery
- [ ] Database backup scheduled daily: `crontab -e`
- [ ] Backup retention policy: 30 days minimum
- [ ] Backup tested (can restore successfully)
- [ ] Application version tagged in git
- [ ] Rollback plan documented

## Documentation & Handover
- [ ] README updated with production URL
- [ ] Admin credentials secured (not in code/logs)
- [ ] SSL certificate renewal process documented
- [ ] Emergency contact list established
- [ ] Runbook for common troubleshooting created
- [ ] Access credentials stored securely (1Password, Vault, etc.)

## Post-Deployment (24hr+ Monitoring)
- [ ] Application stability: no crashes or errors
- [ ] Certificate renewal cron runs (watch logs for 60+ days)
- [ ] All endpoints responding within SLA
- [ ] Error rate < 0.1%
- [ ] No security incidents reported
- [ ] Performance metrics acceptable

## Success Criteria
- ✅ HTTPS working on all endpoints
- ✅ SSL test result: A+ rating
- ✅ Zero security warnings
- ✅ Application response time: <500ms average
- ✅ Error rate: <0.1%
- ✅ Certificate auto-renewal: confirmed working
- ✅ Monitoring & alerting: active

---

**Deployment Date:** _______________  
**Deployed By:** _______________  
**Reviewed By:** _______________  
**Sign-off:** _______________
