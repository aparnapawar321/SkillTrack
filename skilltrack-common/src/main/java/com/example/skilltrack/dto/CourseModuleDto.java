package com.example.skilltrack.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CourseModuleDto {
    
    private Long id;
    
    @NotBlank(message = "Title is required")
    private String title;
    
    private String content;
    
    @NotNull(message = "Order index is required")
    private Integer orderIndex;
}
