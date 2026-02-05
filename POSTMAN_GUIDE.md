# SkillTrack LMS - Postman Collection Guide

## 📦 Files Included

1. **SkillTrack_API_Collection.postman_collection.json** - Complete API collection
2. **SkillTrack_Local.postman_environment.json** - Environment variables for local development

## 🚀 Getting Started

### Step 1: Import into Postman

1. Open Postman
2. Click **Import** button (top left)
3. Drag and drop both JSON files or click **Upload Files**
4. Select both files:
   - `SkillTrack_API_Collection.postman_collection.json`
   - `SkillTrack_Local.postman_environment.json`
5. Click **Import**

### Step 2: Select Environment

1. In the top-right corner, select **SkillTrack LMS - Local** from the environment dropdown
2. This sets `base_url` to `http://localhost:8080`

### Step 3: Start the Application

Make sure the SkillTrack application is running:

```bash
cd /Users/aparnapawar/IdeaProjects/SkillTrack
./gradlew :skilltrack-api:bootRun --args='--spring.profiles.active=dev'
```

Wait for the message: `Started SkillTrackApplication in X seconds`

## 📋 API Endpoints Overview

### Authentication (No Auth Required)
- **Register User** - Create new user account
- **Login** - Authenticate and get JWT token
- **Get Current User** - Get authenticated user profile (requires JWT)

### Courses (JWT Required)
- **Get All Courses** - List all active courses
- **Get Course by ID** - Get course details with modules
- **Create Course** - Create new course (Instructor/Admin only)
- **Update Course** - Update existing course (Instructor/Admin only)
- **Delete Course** - Soft delete course (Instructor/Admin only)

### Enrollments (JWT Required)
- **Enroll in Course** - Enroll in a course
- **Get My Enrollments** - List user's enrollments
- **Update Progress** - Update enrollment progress (0-100%)
- **Unenroll from Course** - Remove enrollment

### Actuator (No Auth Required)
- **Health Check** - Application health status
- **Metrics** - Available metrics
- **Prometheus Metrics** - Prometheus-formatted metrics

## 🔐 Authentication Flow

### Automatic Token Management

The collection includes **automatic JWT token extraction**:

1. When you call **Register User** or **Login**, the response contains a JWT token
2. A test script automatically saves this token to the `jwt_token` variable
3. All subsequent requests automatically use this token in the `Authorization` header

### Manual Token Usage

If needed, you can manually set the token:

1. Go to **Environments** → **SkillTrack LMS - Local**
2. Set the `jwt_token` variable value
3. Save the environment

## 📝 Testing Workflow

### Complete End-to-End Test

Follow these steps in order:

#### 1. Register a New User
```
POST /api/auth/register
```
- Creates a new user with STUDENT role
- Automatically saves JWT token and user ID

#### 2. Login (Optional)
```
POST /api/auth/login
```
- Use if you need to re-authenticate
- Automatically saves JWT token

#### 3. Get Current User
```
GET /api/auth/me
```
- Verifies authentication is working
- Shows user profile and roles

#### 4. Promote User to Instructor (via H2 Console)

To create courses, you need INSTRUCTOR or ADMIN role:

1. Open http://localhost:8080/h2-console
2. Connect: JDBC URL = `jdbc:h2:mem:testdb`, username = `sa`, password = (empty)
3. Run SQL:
   ```sql
   -- Get role IDs
   SELECT * FROM ROLES;
   
   -- Add INSTRUCTOR role to user (replace user_id with your user's ID)
   INSERT INTO USER_ROLES (USER_ID, ROLE_ID) VALUES (1, 2);
   ```

#### 5. Create a Course
```
POST /api/courses
```
- Requires INSTRUCTOR or ADMIN role
- Automatically saves course ID
- Creates course with modules

#### 6. Get All Courses
```
GET /api/courses
```
- Returns list of all courses

#### 7. Get Course by ID
```
GET /api/courses/{{course_id}}
```
- Uses saved course_id variable
- Returns course with modules (cached)

#### 8. Enroll in Course
```
POST /api/enrollments
```
- Enrolls current user in the course
- Automatically saves enrollment ID

#### 9. Get My Enrollments
```
GET /api/enrollments/my
```
- Shows all enrollments for current user

#### 10. Update Progress
```
PUT /api/enrollments/{{enrollment_id}}/progress
```
- Update progress to 50%
- Try again with 100% to mark as completed

#### 11. Update Course
```
PUT /api/courses/{{course_id}}
```
- Modify course details
- Cache is automatically invalidated

#### 12. Unenroll from Course
```
DELETE /api/enrollments/{{enrollment_id}}
```
- Removes enrollment

#### 13. Delete Course
```
DELETE /api/courses/{{course_id}}
```
- Soft deletes the course

## 🔧 Variables Reference

The collection uses these variables (automatically managed):

| Variable | Description | Auto-populated |
|----------|-------------|----------------|
| `base_url` | API base URL | No (set in environment) |
| `jwt_token` | JWT authentication token | Yes (from login/register) |
| `user_id` | Current user ID | Yes (from login/register) |
| `course_id` | Last created course ID | Yes (from create course) |
| `enrollment_id` | Last created enrollment ID | Yes (from enroll) |

## 🎯 Role-Based Access Control

### STUDENT (Default)
- ✅ View courses
- ✅ Enroll/unenroll
- ✅ Update own progress
- ❌ Create/update/delete courses

### INSTRUCTOR
- ✅ All STUDENT permissions
- ✅ Create courses
- ✅ Update courses
- ✅ Delete courses

### ADMIN
- ✅ All INSTRUCTOR permissions
- ✅ Manage users
- ✅ Full system access

## 📊 Response Examples

### Successful Registration
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6MTcwNjg3...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJqb2huX2RvZSIsImlhdCI6...",
  "type": "Bearer",
  "userId": 1,
  "username": "john_doe",
  "email": "john@example.com",
  "roles": ["ROLE_STUDENT"]
}
```

### Course with Modules
```json
{
  "id": 1,
  "title": "Introduction to Spring Boot",
  "description": "Learn Spring Boot fundamentals",
  "instructorId": 1,
  "modules": [
    {
      "id": 1,
      "title": "Getting Started",
      "content": "Introduction to Spring Boot framework",
      "orderIndex": 1
    },
    {
      "id": 2,
      "title": "Spring Data JPA",
      "content": "Working with databases",
      "orderIndex": 2
    }
  ],
  "createdAt": "2026-02-02T17:00:00",
  "updatedAt": "2026-02-02T17:00:00"
}
```

### Enrollment with Progress
```json
{
  "id": 1,
  "userId": 1,
  "courseId": 1,
  "courseTitle": "Introduction to Spring Boot",
  "progress": 50,
  "enrolledAt": "2026-02-02T17:05:00",
  "completedAt": null
}
```

## 🐛 Troubleshooting

### "Unauthorized" Error
- Make sure you've called **Register** or **Login** first
- Check that `jwt_token` variable is populated
- Token expires after 24 hours - login again if needed

### "Forbidden" Error
- You don't have the required role for this endpoint
- Promote user to INSTRUCTOR/ADMIN via H2 Console

### "Connection Refused"
- Make sure the application is running on port 8080
- Check: `curl http://localhost:8080/actuator/health`

### Variables Not Auto-Populating
- Check the **Tests** tab in each request
- Make sure you're using the **SkillTrack LMS - Local** environment

## 🌐 Swagger UI Alternative

You can also test APIs via Swagger UI:
- URL: http://localhost:8080/swagger-ui/index.html
- Click **Authorize** and enter: `Bearer <your-jwt-token>`

## 📚 Additional Resources

- **API Documentation**: See README.md for detailed endpoint descriptions
- **Quick Start Guide**: See QUICKSTART.md for curl examples
- **Walkthrough**: See walkthrough.md for implementation details

---

**Happy Testing! 🚀**
