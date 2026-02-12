package com.example.skilltrack.listener;

import com.example.skilltrack.dto.CourseImportDTO;
import com.example.skilltrack.entity.CourseImportFailure;
import com.example.skilltrack.exception.BatchValidationException;
import com.example.skilltrack.repository.CourseImportFailureRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * SkipListener that captures validation failures and stores them in the database.
 * Ensures the batch job continues processing even when individual records fail.
 */
@Slf4j
@Component
@StepScope
@RequiredArgsConstructor
public class CourseImportSkipListener implements SkipListener<CourseImportDTO, Object> {
    
    private final CourseImportFailureRepository failureRepository;
    
    @Value("#{jobParameters['batchId'] ?: T(java.util.UUID).randomUUID().toString()}")
    private String batchId;
    
    @Override
    public void onSkipInRead(Throwable t) {
        log.warn("Skipped item during read phase: {}", t.getMessage());
        
        // Store read failure
        CourseImportFailure failure = CourseImportFailure.builder()
                .rawData("Unable to read line")
                .errorMessage("Read error: " + t.getMessage())
                .timestamp(LocalDateTime.now())
                .importBatchId(batchId)
                .build();
        
        failureRepository.save(failure);
    }
    
    @Override
    public void onSkipInProcess(CourseImportDTO item, Throwable t) {
        log.warn("Skipped item during process phase - Row {}: {}", 
                item.getRowNumber(), t.getMessage());
        
        // Extract error details
        String errorMessage;
        String rawData = item.getRawData();
        Integer rowNumber = item.getRowNumber();
        
        if (t instanceof BatchValidationException) {
            BatchValidationException ve = (BatchValidationException) t;
            errorMessage = ve.getFormattedErrors();
        } else {
            errorMessage = "Processing error: " + t.getMessage();
        }
        
        // Store processing failure
        CourseImportFailure failure = CourseImportFailure.builder()
                .rawData(rawData)
                .errorMessage(errorMessage)
                .timestamp(LocalDateTime.now())
                .importBatchId(batchId)
                .rowNumber(rowNumber)
                .build();
        
        failureRepository.save(failure);
        
        log.debug("Stored failure record for row {}", rowNumber);
    }
    
    @Override
    public void onSkipInWrite(Object item, Throwable t) {
        log.error("Skipped item during write phase: {}", t.getMessage());
        
        // Store write failure
        CourseImportFailure failure = CourseImportFailure.builder()
                .rawData(item != null ? item.toString() : "Unknown")
                .errorMessage("Write error: " + t.getMessage())
                .timestamp(LocalDateTime.now())
                .importBatchId(batchId)
                .build();
        
        failureRepository.save(failure);
    }
}
