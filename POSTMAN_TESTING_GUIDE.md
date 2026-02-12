# Testing Course Import Batch Job in Postman

This guide explains how to test the course import batch job using Postman.

## Prerequisites

1. **Application Running:** Ensure the SkillTrack API is running on `http://localhost:8080`
2. **Authentication:** You need a valid JWT token with `ADMIN` or `INSTRUCTOR` role
3. **CSV File:** Place a `courses.csv` file in `./data/import/` directory

## Step 1: Get Authentication Token

### Option A: Login with Standard Credentials
```
POST http://localhost:8080/api/auth/login

Body (JSON):
{
  "username": "admin",
  "password": "admin123"
}

Response:
{
  "token": "eyJhbGciOiJIUzUxMiJ9...",
  "type": "Bearer"
}
```

### Option B: Login with GitHub OAuth
1. Visit `http://localhost:8080/oauth2/authorization/github` in browser
2. After redirect, copy the token from the URL parameter
3. Use this token in Postman

## Step 2: Prepare CSV File

Create `./data/import/courses.csv`:
```csv
title,description,instructorId,instructorEmail,modules
"Python Basics","Learn Python",1,,"Intro:Welcome to Python:0|Variables:Learn variables:1"
"Java 101","Java fundamentals",,instructor@example.com,"Setup:Install Java:0|Hello World:First program:1"
```

## Step 3: Import Courses via Postman

### Request Configuration

**Method:** `POST`  
**URL:** `http://localhost:8080/api/batch/import-courses`

**Headers:**
```
Authorization: Bearer YOUR_JWT_TOKEN
Content-Type: application/json
```

**Body (raw JSON):**
```json
{
  "filename": "courses.csv",
  "batchId": "batch-test-001"
}
```

### Expected Response (Success)
```json
{
  "jobExecutionId": 1,
  "batchId": "batch-test-001",
  "status": "STARTED",
  "message": "Course import job started successfully"
}
```

### Expected Response (Error)
```json
{
  "status": "FAILED",
  "message": "Failed to start job: File not found: courses.csv"
}
```

## Step 4: Verify Results

### Check Imported Courses
```
GET http://localhost:8080/api/courses
Authorization: Bearer YOUR_JWT_TOKEN
```

### Check Import Failures (if any)
Query the database:
```sql
SELECT * FROM course_import_failures 
WHERE import_batch_id = 'batch-test-001';
```

## Postman Collection Setup

### Environment Variables
Create a Postman environment with:
```
base_url: http://localhost:8080
token: (your JWT token - set after login)
```

### Collection Structure
```
SkillTrack API
├── Auth
│   ├── Login
│   └── Get Current User
├── Courses
│   ├── Get All Courses
│   └── Get Course by ID
└── Batch Jobs
    ├── Import Courses (minimal)
    ├── Import Courses (with params)
    └── Get Job Status
```

## Testing Scenarios

### Scenario 1: Valid Import
**CSV:**
```csv
title,description,instructorId,instructorEmail,modules
"Test Course","Description",1,,"Module 1:Content:0"
```
**Expected:** Course created successfully

### Scenario 2: Validation Error
**CSV:**
```csv
title,description,instructorId,instructorEmail,modules
"","Missing title",,,"Module:Content:0"
```
**Expected:** Record skipped, logged to `course_import_failures`

### Scenario 3: Multiple Modules
**CSV:**
```csv
title,description,instructorId,instructorEmail,modules
"Advanced Course","Multi-module",1,,"Intro:Start:0|Advanced:Deep dive:1|Conclusion:Summary:2"
```
**Expected:** Course with 3 modules created

## Troubleshooting

### Error: 401 Unauthorized
- **Cause:** Missing or invalid JWT token
- **Fix:** Login again and update the Authorization header

### Error: 403 Forbidden
- **Cause:** User lacks `ADMIN` or `INSTRUCTOR` role
- **Fix:** Login with appropriate user credentials

### Error: File not found
- **Cause:** CSV file not in `./data/import/` directory
- **Fix:** Create the directory and place the CSV file

### Error: Job already running
- **Cause:** Same job parameters used (Spring Batch prevents duplicate runs)
- **Fix:** Use a different `batchId` or wait for current job to complete

## Monitoring Job Execution

### Check Application Logs
```bash
tail -f logs/skilltrack-api.log | grep "course import"
```

### Query Batch Metadata Tables
```sql
-- Latest job executions
SELECT * FROM BATCH_JOB_EXECUTION 
ORDER BY CREATE_TIME DESC LIMIT 5;

-- Step details for specific job
SELECT * FROM BATCH_STEP_EXECUTION 
WHERE JOB_EXECUTION_ID = 1;
```

## Tips

1. **Use unique batch IDs** for each test run
2. **Start with small CSV files** (2-3 records) for testing
3. **Check logs** for detailed error messages
4. **Verify database** after each import
5. **Test validation errors** to ensure skip logic works
