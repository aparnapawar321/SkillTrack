package com.example.skilltrack.controller;

import com.example.skilltrack.dto.EnrollmentDto;
import com.example.skilltrack.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
@Tag(name = "Enrollments", description = "Enrollment management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class EnrollmentController {
    
    private final EnrollmentService enrollmentService;
    
    @PostMapping
    @Operation(summary = "Enroll in course", description = "Enrolls current user in a course")
    public ResponseEntity<EnrollmentDto> enrollInCourse(@RequestBody Map<String, Long> request) {
        Long courseId = request.get("courseId");
        EnrollmentDto enrollment = enrollmentService.enrollInCourse(courseId);
        return new ResponseEntity<>(enrollment, HttpStatus.CREATED);
    }
    
    @DeleteMapping("/{id}")
    @Operation(summary = "Unenroll from course", description = "Removes enrollment from a course")
    public ResponseEntity<Void> unenroll(@PathVariable Long id) {
        enrollmentService.unenroll(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/my")
    @Operation(summary = "Get my enrollments", description = "Returns all enrollments for current user")
    public ResponseEntity<List<EnrollmentDto>> getMyEnrollments() {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments());
    }
    
    @PutMapping("/{id}/progress")
    @Operation(summary = "Update progress", description = "Updates progress for an enrollment")
    public ResponseEntity<EnrollmentDto> updateProgress(@PathVariable Long id, @RequestBody Map<String, Integer> request) {
        Integer progress = request.get("progress");
        return ResponseEntity.ok(enrollmentService.updateProgress(id, progress));
    }
}
