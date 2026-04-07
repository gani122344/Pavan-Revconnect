// ============================================================
// RevConnect — Production CI/CD Pipeline
// Repo: https://github.com/gani122344/Pavan-Revconnect
// Stack: Angular 21 + Spring Boot 3.2 + MySQL (RDS) + S3 + EC2
// ============================================================

pipeline {
    agent any

    environment {
        EC2_IP          = '16.112.195.169'                   // Elastic IP (ap-south-2)
        EC2_CRED_ID     = 'aws-ec2-ssh-key'                // Jenkins SSH credential ID
        RDS_HOST        = credentials('rds-host')          // Jenkins secret: RDS endpoint
        RDS_USER        = credentials('rds-user')          // Jenkins secret: RDS username
        RDS_PASS        = credentials('rds-password')      // Jenkins secret: RDS password
        JWT_SECRET      = credentials('jwt-secret')        // Jenkins secret: JWT key
        S3_BUCKET       = credentials('s3-bucket-name')    // Jenkins secret: S3 bucket
        AWS_REGION      = 'ap-south-2'
    }

    stages {

        // ---- Stage 1: Checkout Code ----
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ---- Stage 2: Build Backend (Spring Boot JAR) ----
        stage('Build Backend') {
            steps {
                dir('backend') {
                    bat 'mvnw.cmd clean package -DskipTests'
                }
            }
        }

        // ---- Stage 3: Build Frontend (Angular Production Build) ----
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    bat 'npm install --legacy-peer-deps'
                    bat 'if exist .angular\\cache rmdir /s /q .angular\\cache'
                    bat 'set NODE_OPTIONS=--max_old_space_size=4096 && npm run build'
                }
            }
        }

        // ---- Stage 4: Deploy to AWS EC2 ----
        stage('Deploy Full Stack to EC2') {
            steps {
                dir('.') {
                    withCredentials([sshUserPrivateKey(credentialsId: "${EC2_CRED_ID}", keyFileVariable: 'SSH_KEY', usernameVariable: 'SSH_USER')]) {
                        powershell '''
                        $ErrorActionPreference = "Stop"
                        $keyPath = "$env:WORKSPACE\\jenkins-key-${env:BUILD_NUMBER}.pem"
                        Copy-Item -Path $env:SSH_KEY -Destination $keyPath -Force
                        icacls $keyPath /inheritance:r /grant:r "NT AUTHORITY\\SYSTEM:F" /grant:r "BUILTIN\\Administrators:F"

                        # Accept SSH host key on first connection
                        $ErrorActionPreference = "Continue"
                        ssh -o StrictHostKeyChecking=accept-new -i $keyPath ${env:SSH_USER}@${env:EC2_IP} "echo connected" 2>&1 | Out-Null
                        $ErrorActionPreference = "Stop"

                        # Allocate 2GB Swap (prevents OOM on t3.micro)
                        ssh -o StrictHostKeyChecking=accept-new -i $keyPath ${env:SSH_USER}@${env:EC2_IP} "sudo fallocate -l 2G /swapfile 2>/dev/null || true; sudo chmod 600 /swapfile 2>/dev/null || true; sudo mkswap /swapfile 2>/dev/null || true; sudo swapon /swapfile 2>/dev/null || true" 2>&1

                        # ---- Backend Deployment ----
                        scp -o StrictHostKeyChecking=accept-new -i $keyPath backend/target/revconnect-1.0.0.jar ${env:SSH_USER}@${env:EC2_IP}:/home/ec2-user/revconnect-1.0.0.jar 2>&1
                        scp -o StrictHostKeyChecking=accept-new -i $keyPath backend/deploy/revconnect-backend.service ${env:SSH_USER}@${env:EC2_IP}:/tmp/ 2>&1
                        ssh -o StrictHostKeyChecking=accept-new -i $keyPath ${env:SSH_USER}@${env:EC2_IP} "mkdir -p /home/ec2-user/revconnect/uploads; sudo mv /tmp/revconnect-backend.service /etc/systemd/system/revconnect-backend.service; sudo chown root:root /etc/systemd/system/revconnect-backend.service; sudo systemctl daemon-reload; sudo systemctl enable revconnect-backend; sudo systemctl restart revconnect-backend" 2>&1

                        # ---- Frontend Deployment ----
                        ssh -o StrictHostKeyChecking=accept-new -i $keyPath ${env:SSH_USER}@${env:EC2_IP} "mkdir -p /tmp/frontend" 2>&1
                        scp -o StrictHostKeyChecking=accept-new -i $keyPath -pr frontend/dist/revconnect-ui/browser/* ${env:SSH_USER}@${env:EC2_IP}:/tmp/frontend/ 2>&1
                        ssh -o StrictHostKeyChecking=accept-new -i $keyPath ${env:SSH_USER}@${env:EC2_IP} "sudo rm -f /etc/nginx/conf.d/revconnect-ssl.conf; sudo rm -rf /var/www/html/revconnect-ui/browser/*; sudo mkdir -p /var/www/html/revconnect-ui/browser/; sudo cp -r /tmp/frontend/* /var/www/html/revconnect-ui/browser/; sudo chown -R ec2-user:ec2-user /var/www/html/revconnect-ui; sudo chmod -R 755 /var/www/html/revconnect-ui/browser; sudo systemctl restart nginx" 2>&1

                        # ---- SSL Certificate Setup ----
                        scp -o StrictHostKeyChecking=accept-new -i $keyPath backend/deploy/ssl-setup.sh ${env:SSH_USER}@${env:EC2_IP}:/tmp/ssl-setup.sh 2>&1
                        ssh -o StrictHostKeyChecking=accept-new -i $keyPath ${env:SSH_USER}@${env:EC2_IP} "sed -i 's/\\r//' /tmp/ssl-setup.sh && chmod +x /tmp/ssl-setup.sh && bash /tmp/ssl-setup.sh" 2>&1

                        # Cleanup SSH key
                        icacls $keyPath /reset
                        Remove-Item -Path $keyPath -Force -ErrorAction SilentlyContinue
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            echo '✅ RevConnect deployed successfully to AWS EC2!'
        }
        failure {
            echo '❌ Deployment failed. Check console output for errors.'
        }
        always {
            echo 'RevConnect CI/CD Pipeline Finished.'
            cleanWs()
        }
    }
}
