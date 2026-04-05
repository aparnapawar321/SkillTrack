package com.example.skilltrack.service;

import com.example.skilltrack.dto.EnrollmentDto;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.entity.Enrollment;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private CourseRepository courseRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private EnrollmentService enrollmentService;

    private User testUser;
    private Course testCourse;
    private Enrollment testEnrollment;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("student")
                .email("student@example.com")
                .build();

        testCourse = Course.builder()
                .id(1L)
                .title("Test Course")
                .instructorId(2L)
                .deleted(false)
                .build();

        testEnrollment = Enrollment.builder()
                .id(1L)
                .user(testUser)
                .course(testCourse)
                .progress(0)
                .build();

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student", null)
        );
    }

    @Test
    void enrollInCourse_WhenNotEnrolled_SavesEnrollment() {
        when(userRepository.findActiveByUsername("student")).thenReturn(Optional.of(testUser));
        when(courseRepository.findById(1L)).thenReturn(Optional.of(testCourse));
        when(enrollmentRepository.existsByUserIdAndCourseId(1L, 1L)).thenReturn(false);
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentDto result = enrollmentService.enrollInCourse(1L);

        assertNotNull(result);
        assertEquals(1L, result.getCourseId());
        verify(enrollmentRepository).save(any(Enrollment.class));
    }

    @Test
    void updateProgress_WhenValid_UpdatesProgress() {
        when(userRepository.findActiveByUsername("student")).thenReturn(Optional.of(testUser));
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(testEnrollment));
        when(enrollmentRepository.save(any(Enrollment.class))).thenReturn(testEnrollment);

        EnrollmentDto result = enrollmentService.updateProgress(1L, 50);

        assertNotNull(result);
        assertEquals(50, testEnrollment.getProgress());
        verify(enrollmentRepository).save(testEnrollment);
    }
}
