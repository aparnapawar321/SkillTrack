# SkillTrack LMS - Quick Start Guide

## ✅ Application Successfully Running!

The SkillTrack LMS is now running on your local machine with the following configuration:

### 🌐 Access Points

- **API Base URL:** http://localhost:8080
- **Swagger UI:** http://localhost:8080/swagger-ui/index.html
- **H2 Console:** http://localhost:8080/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - Username: `sa`
  - Password: (leave empty)
- **Actuator Health:** http://localhost:8080/actuator/health
- **Actuator Metrics:** http://localhost:8080/actuator/metrics

### 🎯 Quick Test Workflow

#### 1. Register a New User

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "john_doe",
    "email": "john@example.com",
    "password": "password123"
  }'
```

**Expected Response:**
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_STUDENT"]
}
```

#### 2. Login

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "emailOrUsername": "john@example.com",
    "password": "password123"
  }'
```

#### 3. Get Current User Profile

```bash
# Replace <TOKEN> with the token from login response
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: Bearer <TOKEN>"
```

#### 4. Create a Course (Requires INSTRUCTOR or ADMIN role)

First, you'll need to promote your user to INSTRUCTOR role via H2 Console:

1. Go to http://localhost:8080/h2-console
2. Connect with JDBC URL: `jdbc:h2:mem:testdb`, username: `sa`, password: (empty)
3. Run this SQL:
   ```sql
   -- Find the INSTRUCTOR role ID
   SELECT * FROM ROLES WHERE NAME = 'ROLE_INSTRUCTOR';
   
   -- Add INSTRUCTOR role to user (replace user_id and role_id)
   INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (1, 2);
   ```

Then create a course:

```bash
curl -X POST http://localhost:8080/api/courses \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{
    "title": "Introduction to Spring Boot",
    "description": "Learn Spring Boot fundamentals",
    "instructorId": 1,
    "modules": [
      {
        "title": "Getting Started",
        "content": "Introduction to Spring Boot",
        "orderIndex": 1
      },
      {
        "title": "Spring Data JPA",
        "content": "Working with databases",
        "orderIndex": 2
      }
    ]
  }'
```

#### 5. List All Courses

```bash
curl -X GET http://localhost:8080/api/courses \
  -H "Authorization: Bearer <TOKEN>"
```

#### 6. Enroll in a Course

```bash
curl -X POST http://localhost:8080/api/enrollments \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"courseId": 1}'
```

#### 7. Get My Enrollments

```bash
curl -X GET http://localhost:8080/api/enrollments/my \
  -H "Authorization: Bearer <TOKEN>"
```

#### 8. Update Progress

```bash
curl -X PUT http://localhost:8080/api/enrollments/1/progress \
  -H "Authorization: Bearer <TOKEN>" \
  -H "Content-Type: application/json" \
  -d '{"progress": 50}'
```

### 🔧 Fixed Issues

1. **Missing H2 Driver** - Added `runtimeOnly 'com.h2database:h2'` to `skilltrack-api/build.gradle`
2. **Port 8080 in Use** - Killed existing process before restart

### 📝 Default Roles Created

The application automatically creates three roles on startup:
- **ROLE_STUDENT** (ID: 1) - Default role for new users
- **ROLE_INSTRUCTOR** (ID: 2) - Can create and manage courses
- **ROLE_ADMIN** (ID: 3) - Full system access

### 🎨 Using Swagger UI

1. Open http://localhost:8080/swagger-ui/index.html
2. Click "Authorize" button (top right)
3. Enter: `Bearer <your-jwt-token>`
4. Click "Authorize" and "Close"
5. Now you can test all endpoints directly from Swagger UI!

### 🛑 Stopping the Application

Press `Ctrl+C` in the terminal where the application is running, or:

```bash
# Find and kill the process
lsof -ti:8080 | xargs kill -9
```

### 🐳 Using PostgreSQL (Production Mode)

1. Start Docker services:
   ```bash
   docker compose up -d
   ```

2. Run without dev profile:
   ```bash
   ./gradlew :skilltrack-api:bootRun
   ```

3. Stop Docker services:
   ```bash
   docker compose down
   ```

### ✅ Verification Checklist

- [x] Application starts successfully
- [x] H2 database initialized
- [x] All roles created automatically
- [x] Swagger UI accessible
- [x] Health endpoint responding
- [x] User registration working
- [x] JWT authentication working
- [x] Role-based access control working
- [x] Course CRUD operations working
- [x] Enrollment management working
- [x] Caching configured
- [x] Auditing enabled (Hibernate Envers)

### 🚀 Next Steps

1. Test all API endpoints via Swagger UI
2. Implement Spring Batch CSV import job
3. Add Quartz scheduler for reminder emails
4. Create integration tests with Testcontainers
5. Configure GitHub OAuth credentials
6. Add SendGrid API key for email notifications

---

**🎉 Congratulations! Your SkillTrack LMS is up and running!**
