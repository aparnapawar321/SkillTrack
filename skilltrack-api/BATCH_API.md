# Batch Job API Documentation

## Import Courses Endpoint

Triggers the course import batch job to process CSV files.

### Endpoint
```
POST /api/batch/import-courses
```

### Authentication
Required roles: `ADMIN` or `INSTRUCTOR`

### Request Body (Optional)
```json
{
  "filename": "courses.csv",
  "batchId": "batch-20260211-001"
}
```

| Field | Type | Required | Default | Description |
|-------|------|----------|---------|-------------|
| `filename` | String | No | `courses.csv` | Name of CSV file in import directory |
| `batchId` | String | No | Auto-generated UUID | Unique identifier for this import batch |

### Response
```json
{
  "jobExecutionId": 123,
  "batchId": "batch-20260211-001",
  "status": "STARTED",
  "message": "Course import job started successfully"
}
```

### Example Requests

#### Minimal Request (uses defaults)
```bash
curl -X POST http://localhost:8080/api/batch/import-courses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json"
```

#### With Custom Parameters
```bash
curl -X POST http://localhost:8080/api/batch/import-courses \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "filename": "courses-2026-02.csv",
    "batchId": "batch-feb-2026"
  }'
```

### Response Codes

| Code | Description |
|------|-------------|
| 200 | Job started successfully |
| 401 | Unauthorized - missing or invalid JWT |
| 403 | Forbidden - user lacks required role |
| 500 | Internal error - job failed to start |

### Error Response
```json
{
  "status": "FAILED",
  "message": "Failed to start job: File not found"
}
```

## Job Status Endpoint

Gets the status of a running or completed batch job.

### Endpoint
```
GET /api/batch/status/{executionId}
```

### Example
```bash
curl http://localhost:8080/api/batch/status/123 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

## Workflow

1. **Prepare CSV file** in `./data/import/` directory
2. **Call API** with optional filename and batchId
3. **Receive response** with job execution ID
4. **Monitor progress** via logs or database queries
5. **Check results:**
   - Valid courses in `courses` table
   - Failures in `course_import_failures` table

## Monitoring Job Execution

### Query Job Status (SQL)
```sql
-- Get latest job executions
SELECT * FROM BATCH_JOB_EXECUTION 
ORDER BY CREATE_TIME DESC 
LIMIT 10;

-- Get step execution details
SELECT * FROM BATCH_STEP_EXECUTION 
WHERE JOB_EXECUTION_ID = 123;
```

### Query Import Failures
```sql
-- Get failures for specific batch
SELECT * FROM course_import_failures 
WHERE import_batch_id = 'batch-20260211-001'
ORDER BY row_number;
```

## Notes

- Each job execution must have unique parameters (ensured by timestamp)
- Jobs run asynchronously - API returns immediately
- Failed records are skipped and logged to database
- Maximum 100 validation errors per job (configurable)
