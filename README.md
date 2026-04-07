# RevConnect

Instagram-like social media application with real-time messaging, stories, and media sharing.

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Frontend** | Angular 21, TypeScript 5.9 |
| **Backend** | Spring Boot 3.2, Java 17 |
| **Database** | MySQL 8 (AWS RDS) |
| **Storage** | AWS S3 (media uploads) |
| **Server** | AWS EC2 (ap-south-1) |
| **CI/CD** | Jenkins Pipeline |
| **Web Server** | Nginx (reverse proxy + SSL) |

## Project Structure

```
revconnect/
├── backend/          # Spring Boot API
│   ├── src/
│   ├── deploy/       # EC2 deployment scripts
│   └── pom.xml
├── frontend/         # Angular SPA
│   ├── src/
│   └── package.json
├── Jenkinsfile       # CI/CD pipeline
└── README.md
```

## Quick Start (Local Development)

### Backend
```bash
cd backend
./mvnw spring-boot:run
# Runs on http://localhost:8080
```

### Frontend
```bash
cd frontend
npm install --legacy-peer-deps
ng serve --proxy-config src/proxy.conf.json
# Runs on http://localhost:4200
```

## Deployment
See `Jenkinsfile` for automated CI/CD pipeline to AWS EC2.
