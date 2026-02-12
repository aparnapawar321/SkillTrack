package com.example.skilltrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for importing courses from CSV files.
 * Maps to CSV columns for batch processing.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseImportDTO {
    
    private String title;
    private String description;
    private Long instructorId;
    private String instructorEmail;
    
    // Modules data - parsed from pipe-separated format in CSV
    @Builder.Default
    private List<CourseModuleImportDTO> modules = new ArrayList<>();
    
    // Additional fields for validation and tracking
    private Integer rowNumber;
    private String rawData;
}

