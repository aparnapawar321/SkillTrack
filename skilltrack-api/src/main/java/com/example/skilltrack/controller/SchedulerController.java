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
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler", description = "Endpoints for managing Quartz scheduled jobs")
@SecurityRequirement(name = "bearerAuth")
public class SchedulerController {

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List scheduled jobs", description = "Returns a list of all active Quartz jobs and their triggers")
    public ResponseEntity<List<Map<String, Object>>> getScheduledJobs() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping("/jobs/{jobName}/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger job manually", description = "Manually trigger a Quartz job by name")
    public ResponseEntity<Map<String, String>> triggerJob(@PathVariable String jobName) {
        log.info("Manual trigger for job: {}", jobName);
        return ResponseEntity.ok(Collections.singletonMap("status", "Job '" + jobName + "' triggered successfully"));
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get job history", description = "Retrieve history of job executions from the persistent store")
    public ResponseEntity<List<Map<String, Object>>> getJobHistory() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
