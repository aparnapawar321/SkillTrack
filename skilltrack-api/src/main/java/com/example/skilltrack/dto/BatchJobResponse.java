package com.example.skilltrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for batch job execution.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobResponse {
    
    private Long jobExecutionId;
    private String batchId;
    private String status;
    private String message;
}
