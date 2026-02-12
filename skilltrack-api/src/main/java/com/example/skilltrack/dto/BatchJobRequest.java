package com.example.skilltrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO for triggering course import batch job.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BatchJobRequest {
    
    private String filename;
    private String batchId;
}
