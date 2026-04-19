#!/bin/bash
# setup-https-prod.sh - Complete HTTPS production setup script
# Usage: sudo bash setup-https-prod.sh yourdomain.com your-email@example.com

set -e

DOMAIN="${1:-yourdomain.com}"
EMAIL="${2:-admin@yourdomain.com}"
APP_USER="appuser"
APP_DIR="/opt/managestore"

if [ -z "$DOMAIN" ] || [ -z "$EMAIL" ]; then
    echo "Usage: sudo bash setup-https-prod.sh <domain> <email>"
    echo "Example: sudo bash setup-https-prod.sh example.com admin@example.com"
    exit 1
fi

echo "=========================================="
echo "ManageStore HTTPS Production Setup"
echo "=========================================="
echo "Domain: $DOMAIN"
echo "Email: $EMAIL"
echo "App Dir: $APP_DIR"
echo ""
read -p "Press Enter to continue..."

# Step 1: Install packages
echo "[1/7] Installing packages..."
apt-get update
apt-get install -y \
    nginx \
    certbot \
    python3-certbot-nginx \
    openjdk-21-jdk \
    curl \
    wget

# Step 2: Create app user
echo "[2/7] Creating application user..."
if ! id -u $APP_USER > /dev/null 2>&1; then
    useradd -r -s /bin/bash -d $APP_DIR $APP_USER
fi

# Step 3: Create directories
echo "[3/7] Creating directories..."
mkdir -p $APP_DIR
mkdir -p /var/log/managestore
mkdir -p /var/www/certbot
chown -R $APP_USER:$APP_USER $APP_DIR /var/log/managestore
chmod 755 $APP_DIR

# Step 4: Get SSL certificate
echo "[4/7] Obtaining SSL certificate from Let's Encrypt..."
systemctl stop nginx
certbot certonly --standalone \
    -d $DOMAIN \
    -d www.$DOMAIN \
    --agree-tos \
    --email $EMAIL \
    --non-interactive \
    --rsa-key-size 4096
systemctl start nginx

# Step 5: Configure Nginx
echo "[5/7] Configuring Nginx..."
cp config/nginx-managestore.conf /etc/nginx/sites-available/managestore
sed -i "s/yourdomain.com/$DOMAIN/g" /etc/nginx/sites-available/managestore
ln -sf /etc/nginx/sites-available/managestore /etc/nginx/sites-enabled/

# Remove default site if exists
rm -f /etc/nginx/sites-enabled/default

# Test Nginx config
nginx -t

# Reload Nginx
systemctl reload nginx

# Step 6: Setup systemd service
echo "[6/7] Setting up systemd service..."
cp config/managestore.service /etc/systemd/system/
systemctl daemon-reload
systemctl enable managestore

# Step 7: Setup certificate renewal
echo "[7/7] Setting up certificate auto-renewal..."
# Create renewal hook script
mkdir -p /etc/letsencrypt/renewal-hooks/post
cat > /etc/letsencrypt/renewal-hooks/post/nginx.sh << 'EOF'
#!/bin/bash
systemctl reload nginx
EOF
chmod +x /etc/letsencrypt/renewal-hooks/post/nginx.sh

# Test renewal (dry run)
certbot renew --dry-run

echo ""
echo "=========================================="
echo "✓ Setup complete!"
echo "=========================================="
echo ""
echo "Next steps:"
echo "1. Copy JAR: cp target/*.jar $APP_DIR/"
echo "2. Start service: systemctl start managestore"
echo "3. Check status: systemctl status managestore"
echo "4. View logs: journalctl -u managestore -f"
echo "5. Test HTTPS: curl -I https://$DOMAIN/api/auth/reset-request"
echo ""
echo "SSL Certificate valid until: $(sudo certbot certificates 2>/dev/null | grep 'Expiry Date')"
echo "=========================================="
