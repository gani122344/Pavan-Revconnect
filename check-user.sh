#!/bin/bash
mysql -h database-1.cr8qqc0gkgm0.ap-south-2.rds.amazonaws.com -u admin --password=Chgani123 revconnect_db -e "SELECT id,username,user_type FROM users WHERE username='ramu';"
