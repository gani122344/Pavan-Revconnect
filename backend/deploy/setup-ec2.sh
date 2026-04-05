#!/bin/bash
# ============================================================
# RevConnect — EC2 Instance Setup Script
# Run this ONCE on your new EC2 instance (Amazon Linux 2023)
# Usage: chmod +x setup-ec2.sh && sudo ./setup-ec2.sh
# ============================================================

set -e

echo "========================================="
echo "  RevConnect EC2 Setup"
echo "========================================="

# --- 1. System Update ---
echo "[1/5] Updating system packages..."
dnf update -y

# --- 2. Install Java 21 ---
echo "[2/5] Installing Java 21 (Amazon Corretto)..."
dnf install -y java-21-amazon-corretto-devel
java -version

# --- 3. Install MySQL 8 ---
echo "[3/5] Installing MySQL 8..."
dnf install -y mariadb105-server
systemctl start mariadb
systemctl enable mariadb

# --- 4. Configure MySQL Database ---
echo "[4/5] Creating database and user..."
mysql -u root <<EOF
CREATE DATABASE IF NOT EXISTS revconnect_db;
ALTER USER 'root'@'localhost' IDENTIFIED BY 'RevConnect2024!';
FLUSH PRIVILEGES;
EOF

echo "  ✅ Database 'revconnect_db' created"
echo "  ✅ Root password set to: RevConnect2024!"

# --- 5. Create app directory ---
echo "[5/5] Setting up application directory..."
mkdir -p /home/ec2-user/revconnect/uploads
chown -R ec2-user:ec2-user /home/ec2-user/revconnect

echo ""
echo "========================================="
echo "  ✅ Setup Complete!"
echo "========================================="
echo ""
echo "  Java:    $(java -version 2>&1 | head -1)"
echo "  MySQL:   $(mysql --version)"
echo "  DB Name: revconnect_db"
echo "  DB User: root"
echo "  DB Pass: RevConnect2024!"
echo ""
echo "  Next step: Upload your JAR and run deploy.sh"
echo "========================================="
