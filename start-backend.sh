#!/bin/bash
pkill -f revconnect-1.0.0.jar 2>/dev/null
sleep 2
truncate -s 0 /home/ec2-user/app.log
/usr/lib/jvm/java-21-amazon-corretto/bin/java -Xmx512m \
  -Duser.timezone=Asia/Kolkata \
  -Dspring.profiles.active=prod \
  -Dspring.datasource.url='jdbc:mysql://revconnect-db.c5qmsgweq0xh.ap-south-1.rds.amazonaws.com:3306/revconnect?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC' \
  -Dspring.datasource.username=admin \
  -Dspring.datasource.password=Chgani123 \
  -Djwt.secret=RevConnectSecretKeyForJWTTokenGenerationMustBeAtLeast256BitsLong2024 \
  -jar /home/ec2-user/revconnect-1.0.0.jar \
  >> /home/ec2-user/app.log 2>&1 &
echo "Backend started"
