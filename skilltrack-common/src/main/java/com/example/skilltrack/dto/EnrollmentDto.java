package com.example.skilltrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnrollmentDto {
    
    private Long id;
    private Long userId;
    private Long courseId;
    private String courseTitle;
    private Integer progress;
    private LocalDateTime enrolledAt;
    private LocalDateTime completedAt;
}
