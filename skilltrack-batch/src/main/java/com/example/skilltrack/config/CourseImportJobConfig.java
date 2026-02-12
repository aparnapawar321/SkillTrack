package com.example.skilltrack.config;

import com.example.skilltrack.dto.CourseImportDTO;
import com.example.skilltrack.entity.Course;
import com.example.skilltrack.exception.BatchValidationException;
import com.example.skilltrack.listener.CourseImportSkipListener;
import com.example.skilltrack.processor.CourseImportProcessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Spring Batch Job configuration for importing courses from CSV files.
 * Implements fault-tolerant processing with skip logic for validation errors.
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class CourseImportJobConfig {
    
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final FlatFileItemReader<CourseImportDTO> courseImportReader;
    private final CourseImportProcessor courseImportProcessor;
    private final JpaItemWriter<Course> courseImportWriter;
    private final CourseImportSkipListener courseImportSkipListener;
    
    /**
     * Defines the course import batch job.
     * 
     * @return configured Job
     */
    @Bean
    public Job courseImportJob() {
        return new JobBuilder("courseImportJob", jobRepository)
                .start(courseImportStep())
                .build();
    }
    
    /**
     * Defines the course import step with fault-tolerant processing.
     * 
     * Configuration:
     * - Chunk size: 10
     * - Skips ValidationException errors
     * - Skip limit: 100 (configurable)
     * - Attaches SkipListener for failure tracking
     * 
     * @return configured Step
     */
    @Bean
    public Step courseImportStep() {
        return new StepBuilder("courseImportStep", jobRepository)
                .<CourseImportDTO, Course>chunk(10, transactionManager)
                .reader(courseImportReader)
                .processor(courseImportProcessor)
                .writer(courseImportWriter)
                // Fault tolerance configuration
                .faultTolerant()
                .skip(BatchValidationException.class)
                .skipLimit(100) // Maximum number of skippable items
                .listener(courseImportSkipListener)
                .build();
    }
}
