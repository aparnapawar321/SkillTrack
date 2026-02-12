package com.example.skilltrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for importing course modules from CSV files.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseModuleImportDTO {
    
    private String title;
    private String content;
    private Integer orderIndex;
}
