package com.example.skilltrack.service;

import com.example.skilltrack.dto.EnrollmentDto;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.entity.Enrollment;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.repository.CourseRepository;
import com.example.skilltrack.repository.EnrollmentRepository;
import com.example.skilltrack.repository.UserRepository;
import com.example.skilltrack.exception.DuplicateEnrollmentException;
import com.example.skilltrack.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {
    
    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final TransactionTemplate transactionTemplate;
    
    @CacheEvict(value = "enrollments", allEntries = true)
    public EnrollmentDto enrollInCourse(Long courseId) {
        return transactionTemplate.execute(status -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userRepository.findActiveByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new RuntimeException("Course not found"));
            
            if (Boolean.TRUE.equals(course.getDeleted())) {
                throw new ValidationException("Cannot enroll in a deleted course");
            }
            
            if (enrollmentRepository.existsByUserIdAndCourseId(user.getId(), courseId)) {
                throw new DuplicateEnrollmentException("User is already enrolled in this course");
            }
            
            Enrollment enrollment = Enrollment.builder()
                    .user(user)
                    .course(course)
                    .progress(0)
                    .build();
            
            Enrollment savedEnrollment = enrollmentRepository.save(enrollment);
            log.info("User {} enrolled in course {}", username, course.getTitle());
            
            return convertToDto(savedEnrollment);
        });
    }
    
    @CacheEvict(value = "enrollments", allEntries = true)
    public void unenroll(Long enrollmentId) {
        transactionTemplate.executeWithoutResult(status -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));
            
            if (!enrollment.getUser().getUsername().equals(username)) {
                throw new RuntimeException("Unauthorized to unenroll from this course");
            }
            
            enrollmentRepository.delete(enrollment);
            log.info("User {} unenrolled from course {}", username, enrollment.getCourse().getTitle());
        });
    }
    
    @Cacheable(value = "enrollments", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public List<EnrollmentDto> getMyEnrollments() {
        return transactionTemplate.execute(status -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userRepository.findActiveByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            return enrollmentRepository.findByUserId(user.getId()).stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        });
    }

    public EnrollmentDto getEnrollmentByCourseId(Long courseId) {
        return transactionTemplate.execute(status -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userRepository.findActiveByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Enrollment enrollment = enrollmentRepository.findByUserIdAndCourseId(user.getId(), courseId)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found for this course"));
                    
            return convertToDto(enrollment);
        });
    }
    
    @CacheEvict(value = "enrollments", allEntries = true)
    public EnrollmentDto updateProgress(Long enrollmentId, Integer progress) {
        return transactionTemplate.execute(status -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            log.info("Updating progress for enrollment {}: User={}, NewProgress={}%", enrollmentId, username, progress);
            
            User currentUser = userRepository.findActiveByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            boolean isInstructor = currentUser.getRoles().stream()
                    .anyMatch(role -> role.getName() == Role.RoleName.ROLE_INSTRUCTOR);
            if (isInstructor) {
                throw new ValidationException("Instructors are not allowed to update progress through this API");
            }

            if (progress == null || progress < 0 || progress > 100) {
                throw new ValidationException("Progress must be between 0 and 100");
            }
            
            Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                    .orElseThrow(() -> new RuntimeException("Enrollment not found"));
            
            if (!enrollment.getUser().getUsername().equals(username)) {
                throw new RuntimeException("Unauthorized to update this enrollment");
            }
            
            if (enrollment.getCompletedAt() != null && progress < 100) {
                throw new ValidationException("Cannot decrease progress for a completed course");
            }
            
            if (enrollment.getCompletedAt() != null && progress == 100) {
                log.info("Progress is already 100% for enrollment {}, skipping update.", enrollmentId);
                return convertToDto(enrollment);
            }
            
            if (progress < enrollment.getProgress()) {
                throw new ValidationException("Progress cannot jump backwards. Current progress: " + enrollment.getProgress() + "%");
            }
            
            enrollment.setProgress(progress);
            if (progress >= 100) {
                enrollment.markAsCompleted();
            }
            
            Enrollment updatedEnrollment = enrollmentRepository.save(enrollment);
            log.info("Updated progress for enrollment {} to {}% for user {}", enrollmentId, progress, username);
            
            return convertToDto(updatedEnrollment);
        });
    }
    
    private EnrollmentDto convertToDto(Enrollment enrollment) {
        return EnrollmentDto.builder()
                .id(enrollment.getId())
                .userId(enrollment.getUser().getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .progress(enrollment.getProgress())
                .enrolledAt(enrollment.getEnrolledAt())
                .completedAt(enrollment.getCompletedAt())
                .build();
    }
}
