#!/bin/bash
# deploy-prod.sh - Production deployment script

set -e  # Exit on error

echo "=== ManageStore Production Deployment Script ==="

# Configuration
DOMAIN="yourdomain.com"
APP_USER="appuser"
APP_DIR="/opt/managestore"
LOG_DIR="/var/log/managestore"
JAR_NAME="ManageStore-0.0.1-SNAPSHOT.jar"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

echo -e "${YELLOW}Step 1: Build application...${NC}"
mvn clean package -DskipTests -Pprod
if [ $? -ne 0 ]; then
    echo -e "${RED}Build failed!${NC}"
    exit 1
fi
echo -e "${GREEN}✓ Build successful${NC}"

echo -e "${YELLOW}Step 2: Create application directory...${NC}"
sudo mkdir -p $APP_DIR $LOG_DIR
sudo chown -R $APP_USER:$APP_USER $APP_DIR $LOG_DIR
echo -e "${GREEN}✓ Directory created${NC}"

echo -e "${YELLOW}Step 3: Stop running service...${NC}"
sudo systemctl stop managestore || true
sleep 2
echo -e "${GREEN}✓ Service stopped${NC}"

echo -e "${YELLOW}Step 4: Copy JAR to server...${NC}"
sudo cp target/$JAR_NAME $APP_DIR/
echo -e "${GREEN}✓ JAR copied${NC}"

echo -e "${YELLOW}Step 5: Start service...${NC}"
sudo systemctl start managestore
sleep 5

# Check service status
if sudo systemctl is-active --quiet managestore; then
    echo -e "${GREEN}✓ Service started successfully${NC}"
else
    echo -e "${RED}✗ Service failed to start${NC}"
    sudo journalctl -u managestore -n 20
    exit 1
fi

echo -e "${YELLOW}Step 6: Verify HTTPS connectivity...${NC}"
RESPONSE=$(curl -s -o /dev/null -w "%{http_code}" https://$DOMAIN/api/auth/reset-request)
if [ "$RESPONSE" = "405" ] || [ "$RESPONSE" = "400" ]; then
    echo -e "${GREEN}✓ HTTPS endpoint responding (HTTP $RESPONSE)${NC}"
else
    echo -e "${RED}✗ Unexpected response code: $RESPONSE${NC}"
fi

echo -e "${YELLOW}Step 7: Check SSL certificate expiry...${NC}"
EXPIRY=$(sudo certbot certificates 2>/dev/null | grep "Expiry Date" | awk '{print $3, $4, $5}')
echo -e "${GREEN}Certificate expires: $EXPIRY${NC}"

echo ""
echo -e "${GREEN}=== Deployment Complete ===${NC}"
echo "Application deployed to: https://$DOMAIN"
echo "Logs: sudo journalctl -u managestore -f"
echo "Status: sudo systemctl status managestore"
