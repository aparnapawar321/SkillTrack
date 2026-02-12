package com.example.skilltrack.processor;

import com.example.skilltrack.dto.CourseImportDTO;
import com.example.skilltrack.dto.CourseModuleImportDTO;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.entity.CourseModule;
import com.example.skilltrack.exception.BatchValidationException;
import com.example.skilltrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Processor for validating and transforming CourseImportDTO to Course entity.
 * Validates all required fields and throws ValidationException for invalid data.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CourseImportProcessor implements ItemProcessor<CourseImportDTO, Course> {
    
    private final UserRepository userRepository;
    @Override
    public Course process(CourseImportDTO dto) throws Exception {
        log.debug("Processing course import: {}", dto.getTitle());
        
        // 1. Resolve instructor if only email is provided
        Long instructorId = dto.getInstructorId();
        if ((instructorId == null || instructorId <= 0) && dto.getInstructorEmail() != null) {
            instructorId = userRepository.findByEmail(dto.getInstructorEmail())
                    .map(com.example.skilltrack.entity.User::getId)
                    .orElse(null);
            
            if (instructorId != null) {
                dto.setInstructorId(instructorId);
                log.debug("Resolved instructor email {} to ID {}", dto.getInstructorEmail(), instructorId);
            }
        }

        // 2. Validate the DTO (now including Resolved ID check)
        validateCourseImport(dto);
        
        // 3. Transform DTO to Entity
        Course course = Course.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .instructorId(dto.getInstructorId())
                .deleted(false)
                .build();
        
        // Process modules if present
        if (dto.getModules() != null && !dto.getModules().isEmpty()) {
            for (CourseModuleImportDTO moduleDto : dto.getModules()) {
                CourseModule module = CourseModule.builder()
                        .title(moduleDto.getTitle())
                        .content(moduleDto.getContent())
                        .orderIndex(moduleDto.getOrderIndex())
                        .build();
                course.addModule(module);
            }
        }
        
        log.debug("Successfully processed course: {}", course.getTitle());
        return course;
    }
    
    /**
     * Validates the course import data.
     * Throws ValidationException if any required fields are missing or invalid.
     */
    private void validateCourseImport(CourseImportDTO dto) throws BatchValidationException {
        List<String> errors = new ArrayList<>();
        
        // Validate course title
        if (dto.getTitle() == null || dto.getTitle().trim().isEmpty()) {
            errors.add("Course title is required");
        } else if (dto.getTitle().length() > 200) {
            errors.add("Course title must not exceed 200 characters");
        }
        
        // Validate instructor (either ID or email must be present)
        if ((dto.getInstructorId() == null || dto.getInstructorId() <= 0) &&
            (dto.getInstructorEmail() == null || dto.getInstructorEmail().trim().isEmpty())) {
            errors.add("Either instructorId or instructorEmail must be provided");
        } else if (dto.getInstructorId() == null || dto.getInstructorId() <= 0) {
            // If email was provided but ID is still null, it means resolution failed
            errors.add("Instructor with email '" + dto.getInstructorEmail() + "' not found");
        }
        
        // Validate instructor email format if provided
        if (dto.getInstructorEmail() != null && !dto.getInstructorEmail().trim().isEmpty()) {
            if (!isValidEmail(dto.getInstructorEmail())) {
                errors.add("Invalid instructor email format: " + dto.getInstructorEmail());
            }
        }
        
        // Validate modules if present
        if (dto.getModules() != null && !dto.getModules().isEmpty()) {
            validateModules(dto.getModules(), errors);
        }
        
        // Throw exception if any validation errors found
        if (!errors.isEmpty()) {
            throw new BatchValidationException(errors, dto.getRawData(), dto.getRowNumber());
        }
    }
    
    /**
     * Validates module data.
     */
    private void validateModules(List<CourseModuleImportDTO> modules, List<String> errors) {
        for (int i = 0; i < modules.size(); i++) {
            CourseModuleImportDTO module = modules.get(i);
            String prefix = "Module " + (i + 1) + ": ";
            
            // Validate module title
            if (module.getTitle() == null || module.getTitle().trim().isEmpty()) {
                errors.add(prefix + "title is required");
            } else if (module.getTitle().length() > 200) {
                errors.add(prefix + "title must not exceed 200 characters");
            }
            
            // Validate module content
            if (module.getContent() == null || module.getContent().trim().isEmpty()) {
                errors.add(prefix + "content is required");
            }
            
            // Validate module orderIndex
            if (module.getOrderIndex() == null) {
                errors.add(prefix + "orderIndex is required");
            } else if (module.getOrderIndex() < 0) {
                errors.add(prefix + "orderIndex must be non-negative");
            }
        }
        
        // Validate orderIndex uniqueness
        long distinctOrderIndexes = modules.stream()
                .map(CourseModuleImportDTO::getOrderIndex)
                .distinct()
                .count();
        
        if (distinctOrderIndexes != modules.size()) {
            errors.add("Module orderIndex values must be unique");
        }
    }
    
    /**
     * Simple email validation.
     */
    private boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        // Basic email pattern
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        return email.matches(emailRegex);
    }
}
