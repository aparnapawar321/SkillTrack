# Course Import Batch Job

This document describes the Spring Batch job for importing courses from CSV files.

## Job Overview

**Job Name:** `courseImportJob`  
**Step Name:** `courseImportStep`

## Configuration

### Chunk Processing
- **Chunk Size:** 10 records per transaction
- **Processing Model:** Read → Process → Write

### Fault Tolerance
- **Skip Policy:** Skips `ValidationException` errors
- **Skip Limit:** 100 failed records maximum
- **Behavior:** Job continues processing even when individual records fail

### Components

| Component | Implementation | Description |
|-----------|---------------|-------------|
| **Reader** | `FlatFileItemReader<CourseImportDTO>` | Reads CSV file and maps to DTO |
| **Processor** | `CourseImportProcessor` | Validates and transforms DTO to entity |
| **Writer** | `JpaItemWriter<Course>` | Persists valid courses to database |
| **Listener** | `CourseImportSkipListener` | Captures and stores validation failures |

## Running the Job

### Via Command Line
```bash
java -jar skilltrack-batch.jar \
  --spring.batch.job.names=courseImportJob \
  --batchId=batch-$(date +%s) \
  --course.import.filename=courses.csv
```

### Via REST API (if exposed)
```bash
POST /api/batch/jobs/courseImportJob
{
  "batchId": "batch-20260211-001",
  "filename": "courses.csv"
}
```

### Job Parameters

| Parameter | Type | Required | Default | Description |
|-----------|------|----------|---------|-------------|
| `batchId` | String | No | Auto-generated UUID | Unique identifier for this import batch |
| `course.import.filename` | String | No | `courses.csv` | Name of the CSV file to import |

## Error Handling

### Validation Failures
When a record fails validation:
1. `ValidationException` is thrown by the processor
2. Spring Batch skips the record (up to skip limit)
3. `CourseImportSkipListener` captures the error
4. Failure is stored in `course_import_failures` table
5. Processing continues with next record

### Failure Record Contents
- `raw_data` - Original CSV line
- `error_message` - Validation error details
- `timestamp` - When the failure occurred
- `import_batch_id` - Links to the batch run
- `row_number` - CSV row number (1-indexed, excluding header)

### Querying Failures
```sql
-- Get all failures for a specific batch
SELECT * FROM course_import_failures 
WHERE import_batch_id = 'batch-20260211-001'
ORDER BY row_number;

-- Count failures by batch
SELECT import_batch_id, COUNT(*) as failure_count
FROM course_import_failures
GROUP BY import_batch_id;
```

## Monitoring

### Job Execution Status
Spring Batch stores execution metadata in:
- `BATCH_JOB_INSTANCE`
- `BATCH_JOB_EXECUTION`
- `BATCH_STEP_EXECUTION`

### Key Metrics
- **Read Count** - Total records read from CSV
- **Write Count** - Successfully imported courses
- **Skip Count** - Failed validation records
- **Commit Count** - Number of chunks committed

## Example Workflow

1. **Prepare CSV file** in `./data/import/courses.csv`
2. **Run the job** with unique batch ID
3. **Monitor execution** via logs or database
4. **Check results:**
   - Valid courses in `courses` table
   - Failures in `course_import_failures` table
5. **Review failures** and fix data issues
6. **Re-run** with corrected data

## Best Practices

1. **Use unique batch IDs** for each import run
2. **Review failures** after each import
3. **Keep CSV files** for audit trail
4. **Monitor skip count** - high skip rates indicate data quality issues
5. **Set appropriate skip limit** based on expected data quality
