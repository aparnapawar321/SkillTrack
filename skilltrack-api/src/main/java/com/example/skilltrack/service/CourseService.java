package com.example.skilltrack.service;

import com.example.skilltrack.dto.CourseDto;
import com.example.skilltrack.dto.CourseModuleDto;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.entity.CourseModule;
import com.example.skilltrack.repository.CourseRepository;
import com.example.skilltrack.repository.EnrollmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import com.example.skilltrack.repository.UserRepository;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseService {
    
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    
    @Transactional(readOnly = true)
    public List<CourseDto> getAllCourses() {
        return courseRepository.findAllByDeletedFalse().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    @Cacheable(value = "courses", key = "#id")
    public CourseDto getCourseById(Long id) {
        Course course = courseRepository.findActiveById(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        return convertToDto(course);
    }
    
    @Transactional
    @CacheEvict(value = "courses", allEntries = true)
    public CourseDto createCourse(CourseDto courseDto) {
        // 1. Get current authenticated user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Full authentication is required to access this resource");
        }
        
        String currentUsername = authentication.getName();
        User currentUser = userRepository.findActiveByUsername(currentUsername)
                .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
        
        // 2. Validate Instructor ID existence and role
        User assignedInstructor = userRepository.findById(courseDto.getInstructorId())
                .orElseThrow(() -> new RuntimeException("Assigned instructor not found with ID: " + courseDto.getInstructorId()));
        
        boolean isValidInstructor = assignedInstructor.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_INSTRUCTOR || r.getName() == Role.RoleName.ROLE_ADMIN);
        
        if (!isValidInstructor) {
            throw new RuntimeException("The user with ID " + courseDto.getInstructorId() + " does not have an Instructor or Admin role.");
        }
        
        // 3. Ownership check: Instructors can only create courses for DISCONNECTED themselves
        // Admins can create courses for any valid instructor
        if (!isAdmin && !currentUser.getId().equals(courseDto.getInstructorId())) {
            throw new AccessDeniedException("You are not authorized to create a course for another instructor.");
        }

        Course course = Course.builder()
                .title(courseDto.getTitle())
                .description(courseDto.getDescription())
                .instructorId(courseDto.getInstructorId())
                .build();
        
        if (courseDto.getModules() != null) {
            courseDto.getModules().forEach(moduleDto -> {
                CourseModule module = CourseModule.builder()
                        .title(moduleDto.getTitle())
                        .content(moduleDto.getContent())
                        .orderIndex(moduleDto.getOrderIndex())
                        .build();
                course.addModule(module);
            });
        }
        
        Course savedCourse = courseRepository.save(course);
        log.info("Created course: {}", savedCourse.getTitle());
        return convertToDto(savedCourse);
    }
    
    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public CourseDto updateCourse(Long id, CourseDto courseDto) {
        Course course = courseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Security check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findActiveByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
        
        if (!isAdmin && !course.getInstructorId().equals(currentUser.getId())) {
            throw new AccessDeniedException("You are not authorized to update this course. Only the assigned instructor or an admin can do so.");
        }
        
        course.setTitle(courseDto.getTitle());
        course.setDescription(courseDto.getDescription());
        
        Course updatedCourse = courseRepository.save(course);
        log.info("Updated course: {}", updatedCourse.getTitle());
        return convertToDto(updatedCourse);
    }
    
    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public void deleteCourse(Long id) {
        Course course = courseRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new RuntimeException("Course not found"));
        
        // Security check
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = userRepository.findActiveByUsername(authentication.getName())
                .orElseThrow(() -> new AccessDeniedException("User not found"));
        
        boolean isAdmin = currentUser.getRoles().stream()
                .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN);
        
        if (!isAdmin) {
            if (!course.getInstructorId().equals(currentUser.getId())) {
                throw new AccessDeniedException("You are not authorized to delete this course. Only the assigned instructor or an admin can do so.");
            }
            
            // Check for active enrollments
            boolean hasActiveEnrollments = enrollmentRepository.existsByCourseIdAndCompletedAtIsNull(id);
            if (hasActiveEnrollments) {
                throw new IllegalStateException("Cannot delete course with active enrollments. Finish or cancel enrollments first.");
            }
        }
        
        course.setDeleted(true);
        courseRepository.save(course);
        log.info("Soft deleted course: {}", course.getTitle());
    }
    
    @Transactional
    @CacheEvict(value = "courses", key = "#id")
    public CourseDto restoreCourse(Long id) {
        Course course = courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with ID: " + id));
        
        if (course.getDeleted() == null || !course.getDeleted()) {
            throw new RuntimeException("Course is not deleted");
        }
        
        course.setDeleted(false);
        Course restoredCourse = courseRepository.save(course);
        log.info("Restored course: {}", restoredCourse.getTitle());
        return convertToDto(restoredCourse);
    }
    
    private CourseDto convertToDto(Course course) {
        List<CourseModuleDto> moduleDtos = course.getModules().stream()
                .map(module -> CourseModuleDto.builder()
                        .id(module.getId())
                        .title(module.getTitle())
                        .content(module.getContent())
                        .orderIndex(module.getOrderIndex())
                        .build())
                .collect(Collectors.toList());
        
        return CourseDto.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .instructorId(course.getInstructorId())
                .modules(moduleDtos)
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
