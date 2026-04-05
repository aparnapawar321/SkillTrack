package com.example.skilltrack.controller;
import com.example.skilltrack.scheduler.service.EmailService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/scheduler")
@RequiredArgsConstructor
@Tag(name = "Scheduler", description = "Endpoints for managing Quartz scheduled jobs")
@SecurityRequirement(name = "bearerAuth")
public class SchedulerController {

    private final Scheduler scheduler;
    private final EmailService emailService;

    @GetMapping("/jobs")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List scheduled jobs", description = "Returns a list of all active Quartz jobs and their triggers")
    public ResponseEntity<List<Map<String, Object>>> getScheduledJobs() {
        try {
            List<Map<String, Object>> jobs = new ArrayList<>();
            for (String groupName : scheduler.getJobGroupNames()) {
                for (JobKey jobKey : scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupName))) {
                    Map<String, Object> jobData = new HashMap<>();
                    jobData.put("name", jobKey.getName());
                    jobData.put("group", jobKey.getGroup());
                    
                    List<Trigger> triggers = (List<Trigger>) scheduler.getTriggersOfJob(jobKey);
                    List<Map<String, Object>> triggerDetails = new ArrayList<>();
                    for (Trigger trigger : triggers) {
                        Map<String, Object> triggerData = new HashMap<>();
                        triggerData.put("name", trigger.getKey().getName());
                        triggerData.put("nextFireTime", trigger.getNextFireTime());
                        triggerData.put("state", scheduler.getTriggerState(trigger.getKey()));
                        triggerDetails.add(triggerData);
                    }
                    jobData.put("triggers", triggerDetails);
                    jobs.add(jobData);
                }
            }
            return ResponseEntity.ok(jobs);
        } catch (SchedulerException e) {
            log.error("Failed to fetch scheduled jobs", e);
            return ResponseEntity.internalServerError().body(null);
        }
    }

    @PostMapping("/jobs/{jobName}/trigger")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Trigger job manually", description = "Manually trigger a Quartz job by name")
    public ResponseEntity<Map<String, String>> triggerJob(@PathVariable String jobName) {
        try {
            log.info("Manual trigger for job: {}", jobName);
            scheduler.triggerJob(new JobKey(jobName));
            return ResponseEntity.ok(Collections.singletonMap("status", "Job '" + jobName + "' triggered successfully"));
        } catch (SchedulerException e) {
            log.error("Failed to trigger job: {}", jobName, e);
            return ResponseEntity.internalServerError().body(Collections.singletonMap("error", "Failed: " + e.getMessage()));
        }
    }

    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get job history", description = "Retrieve history of job executions from the persistent store")
    public ResponseEntity<List<Map<String, Object>>> getJobHistory() {
        return ResponseEntity.ok(Collections.emptyList());
    }

    @PostMapping("/test-email")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Send test email", description = "Sends a test email via SendGrid to verify configuration")
    public ResponseEntity<Map<String, String>> sendTestEmail(@RequestParam String email) {
        log.info("Manual test email request for: {}", email);
        emailService.sendReminderEmail(email, "Test Course (SendGrid Discovery)");
        return ResponseEntity.ok(Collections.singletonMap("status", "Test email sent to " + email + ". Please check logs for delivery status."));
    }
}
