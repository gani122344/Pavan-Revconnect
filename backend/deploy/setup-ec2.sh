#!/bin/bash
# ============================================================
# RevConnect — EC2 Instance Setup Script (RDS Edition)
# Run this ONCE on your new EC2 instance (Amazon Linux 2023)
# Usage: chmod +x setup-ec2.sh && sudo ./setup-ec2.sh
# ============================================================

set -e

echo "========================================="
echo "  RevConnect EC2 Setup (RDS Edition)"
echo "========================================="

# --- 1. System Update ---
echo "[1/5] Updating system packages..."
dnf update -y

# --- 2. Install Java 21 ---
echo "[2/5] Installing Java 21 (Amazon Corretto)..."
dnf install -y java-21-amazon-corretto-devel
java -version

# --- 3. Install MySQL Client (to connect to RDS) ---
echo "[3/5] Installing MySQL client..."
dnf install -y mariadb105
echo "  ✅ MySQL client installed (for RDS connectivity testing)"

# --- 4. Install Nginx ---
echo "[4/5] Installing Nginx..."
dnf install -y nginx
systemctl start nginx
systemctl enable nginx

# --- 5. Create app directory ---
echo "[5/5] Setting up application directory..."
mkdir -p /home/ec2-user/revconnect/uploads
mkdir -p /var/www/html/revconnect-ui/browser
chown -R ec2-user:ec2-user /home/ec2-user/revconnect
chown -R ec2-user:ec2-user /var/www/html/revconnect-ui

echo ""
echo "========================================="
echo "  ✅ Setup Complete!"
echo "========================================="
echo ""
echo "  Java:    $(java -version 2>&1 | head -1)"
echo "  MySQL:   $(mysql --version) (client only — DB is on RDS)"
echo ""
echo "  RDS Host: database-1.cr8qqc0gkgm0.ap-south-2.rds.amazonaws.com"
echo "  RDS User: admin"
echo "  DB Name:  revconnect_db"
echo ""
echo "  Test RDS: mysql -h database-1.cr8qqc0gkgm0.ap-south-2.rds.amazonaws.com -u admin -p"
echo ""
echo "  Next step: Upload your JAR and deploy"
echo "========================================="
