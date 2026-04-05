package com.example.skilltrack.processor;

import com.example.skilltrack.dto.CourseImportDTO;
import com.example.skilltrack.dto.CourseModuleImportDTO;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.exception.BatchValidationException;
import com.example.skilltrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseImportProcessorTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CourseImportProcessor processor;

    private CourseImportDTO validDto;

    @BeforeEach
    void setUp() {
        validDto = CourseImportDTO.builder()
                .title("New Course")
                .description("Course Description")
                .instructorId(1L)
                .modules(new java.util.ArrayList<>())
                .build();
    }

    @Test
    void process_WithValidId_ReturnsCourse() throws Exception {
        Course result = processor.process(validDto);

        assertNotNull(result);
        assertEquals("New Course", result.getTitle());
        assertEquals(1L, result.getInstructorId());
    }

    @Test
    void process_WithEmailResolution_ReturnsCourse() throws Exception {
        CourseImportDTO dto = CourseImportDTO.builder()
                .title("Email Course")
                .instructorEmail("instructor@example.com")
                .build();
        
        User instructor = User.builder().id(10L).email("instructor@example.com").build();
        when(userRepository.findByEmail("instructor@example.com")).thenReturn(Optional.of(instructor));

        Course result = processor.process(dto);

        assertNotNull(result);
        assertEquals(10L, result.getInstructorId());
    }

    @Test
    void process_WithInvalidTitle_ThrowsException() {
        validDto.setTitle("");
        
        assertThrows(BatchValidationException.class, () -> processor.process(validDto));
    }

    @Test
    void process_WithDuplicateModuleOrderIndex_ThrowsException() {
        validDto.getModules().add(CourseModuleImportDTO.builder().title("M1").content("C1").orderIndex(1).build());
        validDto.getModules().add(CourseModuleImportDTO.builder().title("M2").content("C2").orderIndex(1).build());
        
        assertThrows(BatchValidationException.class, () -> processor.process(validDto));
    }
}
