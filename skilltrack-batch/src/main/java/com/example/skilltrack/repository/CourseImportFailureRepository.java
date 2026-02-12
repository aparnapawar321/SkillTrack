package com.example.skilltrack.repository;

import com.example.skilltrack.entity.CourseImportFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for CourseImportFailure entity.
 * Tracks failed course imports for debugging and reporting.
 */
@Repository
public interface CourseImportFailureRepository extends JpaRepository<CourseImportFailure, Long> {
    
    /**
     * Find all failures for a specific import batch.
     */
    List<CourseImportFailure> findByImportBatchId(String importBatchId);
    
    /**
     * Find failures within a time range.
     */
    List<CourseImportFailure> findByTimestampBetween(LocalDateTime start, LocalDateTime end);
    
    /**
     * Count failures for a specific batch.
     */
    long countByImportBatchId(String importBatchId);
}
