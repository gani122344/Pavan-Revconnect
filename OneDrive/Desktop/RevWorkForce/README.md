# Rev Workforce

Rev Workforce is a console-based Human Resource Management (HRM) application.

## Features
- **Employee**: Leave management (Apply, Cancel, History), Performance (Goals, Reviews), Profile Management.
- **Manager**: Team Management (Balances, Profiles), Leave Approval/Rejection, Performance Feedback & Summary.
- **Admin**: Employee Management, System Config, Leave Revocation & Reports, Audit Logs.

## Technology Stack
- Java 17
- MySQL 8.0
- Log4j 2
- JUnit 5
- Maven

## Setup
1. **Database**: Run `schema.sql` in your MySQL database to create tables.
   ```bash
   mysql -u root -p < schema.sql
   ```
2. **Configuration**: Ensure `DBConnection.java` (in `src/main/java/com/revworkforce/util`) points to your database credentials via `application.properties`.

## Build and Run
Build the project using Maven:
```bash
mvn clean package
```

Run the application:
```bash
java -cp target/revworkforce-1.0-SNAPSHOT.jar com.revworkforce.Main
```
Or use the Maven exec plugin:
```bash
mvn exec:java
```

## Testing
Run unit tests:
```bash
mvn test
```
