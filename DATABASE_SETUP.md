# IT Helpdesk Ticketing System - Database Setup Guide

## Prerequisites
- PostgreSQL 12+ installed
- pgAdmin or psql command-line tool
- Git Bash or PowerShell (Windows)

## Step-by-Step Setup

### Step 1: Create Database and User (as PostgreSQL admin)

```sql
-- Connect as postgres superuser first
-- In pgAdmin: Right-click → Query Tool or use psql

CREATE USER helpdesk_user WITH PASSWORD 'password123';
CREATE DATABASE helpdesk_db OWNER helpdesk_user;

-- Grant privileges
GRANT ALL PRIVILEGES ON DATABASE helpdesk_db TO helpdesk_user;

-- Connect to helpdesk_db and grant schema privileges
\c helpdesk_db
GRANT ALL ON SCHEMA public TO helpdesk_user;
```

### Step 2: Run the Schema Script

**Option A: Using pgAdmin**
1. Open pgAdmin and connect to your PostgreSQL server
2. Select the `helpdesk_db` database
3. Right-click → Query Tool
4. Open and run `database-schema.sql`

**Option B: Using psql (Command Line)**
```bash
# Windows PowerShell
psql -U helpdesk_user -d helpdesk_db -h localhost < database-schema.sql

# Or use:
psql -U helpdesk_user -d helpdesk_db -h localhost
# Then type: \i 'C:/path/to/database-schema.sql'
```

**Option C: Spring Boot Auto-Generation**
The application can auto-create tables. Modify `application.properties`:
```properties
# For development (auto-create/update):
spring.jpa.hibernate.ddl-auto=update

# For production (don't auto-create):
spring.jpa.hibernate.ddl-auto=validate
```

### Step 3: Verify Database Setup

```sql
-- Connect to helpdesk_db
\c helpdesk_db

-- List all tables
\dt

-- Expected output should show:
-- tickets, user, category, ticket_comments, ticket_attachments, 
-- sla_config, ticket_history, knowledge_base

-- Verify sample data
SELECT * FROM category;
SELECT * FROM sla_config;
SELECT * FROM "user"; -- Default admin user
```

### Step 4: Update Spring Boot Application

**application.properties** should already have:
```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/helpdesk_db
spring.datasource.username=helpdesk_user
spring.datasource.password=password123
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect
spring.thymeleaf.cache=false
```

### Step 5: Start the Application

```bash
# Using Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.war
```

The application should start at: `http://localhost:8082`

---

## Database Schema Overview

### Core Tables
- **user** - System users (Admin, Technician, Regular User)
- **category** - Ticket categories (IT Support, Hardware, etc.)
- **tickets** - Main ticket records
- **ticket_comments** - Communication/notes on tickets
- **ticket_attachments** - File uploads for tickets

### Supporting Tables
- **sla_config** - Service Level Agreement settings
- **ticket_history** - Audit trail of all changes
- **knowledge_base** - Help articles and solutions

---

## Default Admin User

After schema creation:
- **Username:** `admin`
- **Password:** `password123` (⚠️ CHANGE THIS IN PRODUCTION!)
- **Email:** admin@helpdesk.com
- **Role:** ADMIN

---

## Important Notes

1. **Password Security:** The default admin password is hashed using bcrypt. In production, use a strong password.

2. **Email Configuration:** Add SMTP settings to `application.properties` for email notifications:
   ```properties
   spring.mail.host=smtp.gmail.com
   spring.mail.port=587
   spring.mail.username=your-email@gmail.com
   spring.mail.password=your-app-password
   spring.mail.properties.mail.smtp.auth=true
   spring.mail.properties.mail.smtp.starttls.enable=true
   ```

3. **File Uploads:** Create a directory for file storage:
   ```bash
   mkdir C:\helpdesk\uploads
   ```
   Update `application.properties`:
   ```properties
   app.upload.dir=C:/helpdesk/uploads
   ```

4. **Backup:** Regularly backup your database:
   ```bash
   pg_dump -U helpdesk_user -d helpdesk_db > backup.sql
   ```

---

## Troubleshooting

**Error: "role 'helpdesk_user' does not exist"**
- Make sure you created the user and database as shown in Step 1

**Connection refused**
- Check if PostgreSQL is running
- Verify host, port, username, and password in application.properties
- Check firewall settings

**Tables not created**
- Set `spring.jpa.hibernate.ddl-auto=create` temporarily to force creation
- Check application logs for SQL errors
- Verify database user has proper permissions
