package com.example.skilltrack.controller;

import com.example.skilltrack.dto.CourseDto;
import com.example.skilltrack.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
@Tag(name = "Courses", description = "Course management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CourseController {
    
    private final CourseService courseService;
    
    @GetMapping
    @Operation(summary = "Get all courses", description = "Returns list of all active courses")
    public ResponseEntity<List<CourseDto>> getAllCourses() {
        return ResponseEntity.ok(courseService.getAllCourses());
    }
    
    @GetMapping("/{id}")
    @Operation(summary = "Get course by ID", description = "Returns course details with modules")
    public ResponseEntity<CourseDto> getCourseById(@PathVariable Long id) {
        return ResponseEntity.ok(courseService.getCourseById(id));
    }
    
    @PostMapping
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Create course", description = "Creates a new course (Instructor/Admin only)")
    public ResponseEntity<CourseDto> createCourse(@Valid @RequestBody CourseDto courseDto) {
        CourseDto created = courseService.createCourse(courseDto);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }
    
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Update course", description = "Updates an existing course (Instructor/Admin only)")
    public ResponseEntity<CourseDto> updateCourse(@PathVariable Long id, @Valid @RequestBody CourseDto courseDto) {
        return ResponseEntity.ok(courseService.updateCourse(id, courseDto));
    }
    
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('INSTRUCTOR', 'ADMIN')")
    @Operation(summary = "Delete course", description = "Soft deletes a course (Instructor/Admin only)")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }
    
    @GetMapping("/{id}/progress")
    @Operation(summary = "Get course progress", description = "Returns current user's enrollment progress for the course")
    public ResponseEntity<com.example.skilltrack.dto.EnrollmentDto> getCourseProgress(@PathVariable Long id, @org.springframework.beans.factory.annotation.Autowired com.example.skilltrack.service.EnrollmentService enrollmentService) {
        return ResponseEntity.ok(enrollmentService.getEnrollmentByCourseId(id));
    }
    
    @PutMapping("/{id}/progress")
    @Operation(summary = "Update course progress", description = "Updates progress for the current user's enrollment in this course")
    public ResponseEntity<com.example.skilltrack.dto.EnrollmentDto> updateCourseProgress(@PathVariable Long id, @RequestBody java.util.Map<String, Integer> request, @org.springframework.beans.factory.annotation.Autowired com.example.skilltrack.service.EnrollmentService enrollmentService) {
        Integer progress = request.get("progress");
        com.example.skilltrack.dto.EnrollmentDto enrollment = enrollmentService.getEnrollmentByCourseId(id);
        return ResponseEntity.ok(enrollmentService.updateProgress(enrollment.getId(), progress));
    }
}
