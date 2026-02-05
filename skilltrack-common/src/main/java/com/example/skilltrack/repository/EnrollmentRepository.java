package com.example.skilltrack.repository;

import com.example.skilltrack.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    
    List<Enrollment> findByUserId(Long userId);
    
    List<Enrollment> findByCourseId(Long courseId);
    
    Optional<Enrollment> findByUserIdAndCourseId(Long userId, Long courseId);
    
    boolean existsByUserIdAndCourseId(Long userId, Long courseId);
    
    boolean existsByCourseIdAndCompletedAtIsNull(Long courseId);
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.completedAt IS NOT NULL")
    long countCompleted();
    
    @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.completedAt IS NOT NULL AND e.completedAt >= :startDate")
    long countCompletedSince(@Param("startDate") LocalDateTime startDate);
    
    @Query("SELECT e FROM Enrollment e WHERE e.completedAt IS NULL AND e.progress < 100")
    List<Enrollment> findIncompleteEnrollments();
}
