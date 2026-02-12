package com.example.skilltrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "course_import_failures")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class CourseImportFailure {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "raw_data", columnDefinition = "TEXT", nullable = false)
    private String rawData;
    
    @Column(name = "error_message", columnDefinition = "TEXT", nullable = false)
    private String errorMessage;
    
    @CreatedDate
    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "import_batch_id")
    private String importBatchId;
    
    @Column(name = "row_number")
    private Integer rowNumber;
}
