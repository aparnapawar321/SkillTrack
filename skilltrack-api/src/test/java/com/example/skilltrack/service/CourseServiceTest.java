package com.example.skilltrack.service;

import com.example.skilltrack.dto.CourseDto;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.repository.CourseRepository;
import com.example.skilltrack.repository.EnrollmentRepository;
import com.example.skilltrack.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @InjectMocks
    private CourseService courseService;

    private User adminUser;
    private Course testCourse;

    @BeforeEach
    void setUp() {
        adminUser = User.builder()
                .id(1L)
                .username("admin")
                .email("admin@example.com")
                .roles(Collections.singleton(Role.builder().name(Role.RoleName.ROLE_ADMIN).build()))
                .build();

        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .description("Test Description")
                .instructorId(1L)
                .deleted(false)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("admin", null)
        );
    }

    @Test
    void getCourseById_WhenExists_ReturnsDto() {
        when(courseRepository.findActiveById(1L)).thenReturn(Optional.of(testCourse));

        CourseDto result = courseService.getCourseById(1L);

        assertNotNull(result);
        assertEquals(testCourse.getTitle(), result.getTitle());
        verify(courseRepository).findActiveById(1L);
    }

    @Test
    void deleteCourse_WhenAdmin_SoftDeletesCourse() {
        when(courseRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testCourse));
        when(userRepository.findActiveByUsername("admin")).thenReturn(Optional.of(adminUser));

        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        courseService.deleteCourse(1L);

        assertTrue(testCourse.getDeleted());
        verify(courseRepository).save(testCourse);
    }

    @Test
    void restoreCourse_WhenDeleted_RestoresCourse() {
        testCourse.setDeleted(true);
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(courseRepository.save(any(Course.class))).thenReturn(testCourse);
        CourseDto result = courseService.restoreCourse(1L);

        assertFalse(testCourse.getDeleted());
        verify(courseRepository).save(testCourse);
        assertNotNull(result);
    }
}
