package com.example.skilltrack.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.launch.JobLauncher;

@Slf4j
@RestController
@RequestMapping("/api/batch")
@Tag(name = "Batch Jobs", description = "Endpoints for manual batch job orchestration")
@SecurityRequirement(name = "bearerAuth")
@RequiredArgsConstructor
public class BatchController {

    private final JobLauncher jobLauncher;
    private final Job courseImportJob;

    @PostMapping("/import-courses")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Trigger course import", description = "Manually trigger the Spring Batch job to import courses from CSV")
    public ResponseEntity<Map<String, String>> triggerCourseImport(@RequestBody(required = false) Map<String, String> request) {
        try {
            String filename = (request != null && request.get("filename") != null) ? request.get("filename") : "courses.csv";
            String batchId = (request != null && request.get("batchId") != null) ? request.get("batchId") : "batch-" + java.util.UUID.randomUUID();

            log.info("Manual course import triggered - batchId: {}, filename: {}", batchId, filename);

            org.springframework.batch.core.JobParameters jobParameters = new org.springframework.batch.core.JobParametersBuilder()
                    .addString("batchId", batchId)
                    .addString("course.import.filename", filename)
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();

            org.springframework.batch.core.JobExecution jobExecution = jobLauncher.run(courseImportJob, jobParameters);

            Map<String, String> response = new java.util.HashMap<>();
            response.put("jobExecutionId", jobExecution.getId().toString());
            response.put("batchId", batchId);
            response.put("status", jobExecution.getStatus().name());
            response.put("message", "Batch job 'courseImport' triggered successfully");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Failed to trigger course import", e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Failed to start job: " + e.getMessage()));
        }
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "List job executions", description = "Get status of recent batch job executions")
    public ResponseEntity<List<Map<String, Object>>> getJobExecutions() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
