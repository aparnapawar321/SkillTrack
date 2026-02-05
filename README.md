# SkillTrack
A modular Learning Management System (LMS) where developers can register, enroll in courses, complete coding challenges, and track their progress.  The focus is on enterprise-grade backend design, leveraging advanced Java and Spring capabilities (persistence, scheduling, security, caching, testing, etc.). 
=======
# SkillTrack LMS - Learning Management System

[![Java](https://img.shields.io/badge/Java-17-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Gradle](https://img.shields.io/badge/Gradle-8.5-blue.svg)](https://gradle.org/)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

## 📋 Overview

SkillTrack is an enterprise-grade Learning Management System (LMS) built with advanced Spring Boot features. It demonstrates mastery of modern Java and Spring technologies including JPA/Hibernate, Spring Security with JWT and OAuth2, Spring Batch, Quartz Scheduler, caching, and comprehensive testing.

## 🎯 Key Features

### 1. **User Management**
- User registration and authentication with JWT tokens
- OAuth2 login with GitHub
- Role-based access control (Admin, Instructor, Student)
- Role hierarchy: ADMIN > INSTRUCTOR > STUDENT
- Password encryption with BCrypt
- Soft delete support for users

### 2. **Course & Enrollment**
- Complete CRUD operations for courses and modules
- Entity graphs for optimized database queries
- Soft delete functionality
- Progress tracking (0-100%)
- Enrollment management

### 3. **Spring Batch Processing**
- CSV import for course metadata
- Validation and error handling
- Failed record logging
- Manual and scheduled job triggers

### 4. **Quartz Scheduler**
- Daily reminder emails for incomplete courses
- Persistent job store (database-backed)
- Job history tracking
- Manual job triggering via REST API

### 5. **Caching & Performance**
- Caffeine cache for courses, instructors, and users
- Configurable TTL (10 minutes)
- Cache invalidation on updates
- Redis support (optional)

### 6. **Audit & Notifications**
- Hibernate Envers for entity change tracking
- Async event processing
- SendGrid integration for email notifications
- Comprehensive audit trail

### 7. **Metrics & Monitoring**
- Spring Actuator endpoints
- Custom Micrometer metrics
- Prometheus integration
- Health checks

### 8. **API Documentation**
- OpenAPI 3.0 (Swagger UI)
- Comprehensive endpoint documentation
- JWT authentication support in Swagger

## 🏗️ Architecture

### Multi-Module Structure

```
SkillTrack/
├── skilltrack-common/          # Shared entities, DTOs, repositories
├── skilltrack-api/             # REST API, controllers, security
├── skilltrack-batch/           # Spring Batch jobs
├── skilltrack-scheduler/       # Quartz scheduler jobs
└── docker-compose.yml          # PostgreSQL & Redis
```

### Technology Stack

- **Java 17** - Modern Java features
- **Spring Boot 3.2.2** - Application framework
- **Spring Data JPA** - Database access with Hibernate
- **Spring Security** - Authentication & authorization
- **JWT (JJWT 0.12.3)** - Stateless authentication
- **OAuth2 Client** - GitHub social login
- **Spring Batch** - Batch processing
- **Quartz** - Job scheduling
- **Caffeine** - In-memory caching
- **Redis** - Distributed caching (optional)
- **Hibernate Envers** - Entity auditing
- **MapStruct** - DTO mapping
- **PostgreSQL** - Primary database
- **H2** - Testing database
- **Testcontainers** - Integration testing
- **SpringDoc OpenAPI** - API documentation
- **Lombok** - Boilerplate reduction

## 🚀 Getting Started

### Prerequisites

- Java 17 or higher
- Docker and Docker Compose
- Gradle 8.5+ (included via wrapper)

### Quick Start

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd SkillTrack
   ```

2. **Start Docker services**
   ```bash
   docker-compose up -d
   ```

3. **Build the project**
   ```bash
   ./gradlew clean build
   ```

4. **Run the application**
   ```bash
   ./gradlew :skilltrack-api:bootRun
   ```

5. **Access the application**
   - API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html
   - H2 Console (dev mode): http://localhost:8080/h2-console
   - Actuator: http://localhost:8080/actuator

### Environment Variables

Create a `.env` file or set environment variables:

```bash
# JWT Configuration
JWT_SECRET=your-secret-key-here

# GitHub OAuth (optional)
GITHUB_CLIENT_ID=your-github-client-id
GITHUB_CLIENT_SECRET=your-github-client-secret

# SendGrid (optional)
SENDGRID_API_KEY=your-sendgrid-api-key

# Email Configuration
EMAIL_ENABLED=false  # Set to true to enable email notifications

# Batch Import Directory
BATCH_IMPORT_DIR=./data/import
```

## 📚 API Endpoints

### Authentication
- `POST /api/auth/register` - Register new user
- `POST /api/auth/login` - Login and get JWT token
- `GET /api/auth/me` - Get current user profile

### Courses (Instructor/Admin)
- `POST /api/courses` - Create course
- `PUT /api/courses/{id}` - Update course
- `DELETE /api/courses/{id}` - Soft delete course
- `GET /api/courses` - List all courses
- `GET /api/courses/{id}` - Get course details

### Enrollments (Student)
- `POST /api/enrollments` - Enroll in course
- `DELETE /api/enrollments/{id}` - Un-enroll from course
- `GET /api/enrollments/my` - Get my enrollments
- `PUT /api/enrollments/{id}/progress` - Update progress

### Batch Jobs (Admin/Instructor)
- `POST /api/batch/import-courses` - Trigger course import
- `GET /api/batch/jobs` - List job executions
- `GET /api/batch/jobs/{id}` - Get job details

### Scheduler (Admin)
- `GET /api/scheduler/jobs` - List scheduled jobs
- `POST /api/scheduler/jobs/{name}/trigger` - Trigger job manually
- `GET /api/scheduler/history` - Get job history

### Admin
- `GET /api/admin/users` - List all users
- `PUT /api/admin/users/{id}/roles` - Update user roles

## 🧪 Testing

### Run All Tests
```bash
./gradlew test
```

### Run Integration Tests
```bash
./gradlew integrationTest
```

### Generate Coverage Report
```bash
./gradlew jacocoTestReport
```

Coverage reports are generated in `build/reports/jacoco/test/html/index.html`

## 🔒 Security

### Role Hierarchy
- **ADMIN** - Full system access
- **INSTRUCTOR** - Can manage courses and view batch jobs
- **STUDENT** - Can enroll in courses and track progress

### Authentication Flow
1. User registers or logs in
2. Server returns JWT access token and refresh token
3. Client includes token in `Authorization: Bearer <token>` header
4. Server validates token on each request

### OAuth2 Login
1. User clicks "Login with GitHub"
2. Redirected to GitHub for authorization
3. On success, user is created/updated in database
4. JWT token is generated and returned

## 📊 Monitoring

### Actuator Endpoints
- `/actuator/health` - Application health
- `/actuator/metrics` - Application metrics
- `/actuator/prometheus` - Prometheus metrics
- `/actuator/scheduledtasks` - Scheduled tasks info

### Custom Metrics
- `enrollments.completed.daily` - Daily completed enrollments
- `users.active` - Active user count

## 🐳 Docker Deployment

### Using Docker Compose
```bash
# Start all services
docker-compose up -d

# View logs
docker-compose logs -f

# Stop services
docker-compose down

# Remove volumes
docker-compose down -v
```

### Services
- **PostgreSQL** - Port 5432
- **Redis** - Port 6379

## 📝 Development

### Running in Development Mode
```bash
# Uses H2 in-memory database
./gradlew :skilltrack-api:bootRun --args='--spring.profiles.active=dev'
```

### Database Migrations
The application uses Hibernate's `ddl-auto=update` for development. For production, consider using Flyway or Liquibase.

### Adding New Modules
1. Create module directory structure
2. Add module to `settings.gradle`
3. Create `build.gradle` for the module
4. Implement functionality

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

