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

@Slf4j
@RestController
@RequestMapping("/api/batch")
@RequiredArgsConstructor
@Tag(name = "Batch Jobs", description = "Endpoints for manual batch job orchestration")
@SecurityRequirement(name = "bearerAuth")
public class BatchController {

    @PostMapping("/import-courses")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Trigger course import", description = "Manually trigger the Spring Batch job to import courses from CSV")
    public ResponseEntity<Map<String, String>> triggerCourseImport() {
        log.info("Manual course import triggered");
        // Implementation will interface with JobLauncher in future steps
        return ResponseEntity.ok(Collections.singletonMap("status", "Batch job 'courseImport' triggered successfully"));
    }

    @GetMapping("/jobs")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "List job executions", description = "Get status of recent batch job executions")
    public ResponseEntity<List<Map<String, Object>>> getJobExecutions() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
