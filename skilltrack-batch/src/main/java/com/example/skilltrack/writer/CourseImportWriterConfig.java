package com.example.skilltrack.writer;

import com.example.skilltrack.entity.Course;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration for writing valid Course entities to the database.
 */
@Slf4j
@Configuration
public class CourseImportWriterConfig {
    
    /**
     * Creates a JpaItemWriter for persisting Course entities.
     * 
     * @param entityManagerFactory the JPA entity manager factory
     * @return configured JpaItemWriter
     */
    @Bean
    public JpaItemWriter<Course> courseImportWriter(EntityManagerFactory entityManagerFactory) {
        JpaItemWriter<Course> writer = new JpaItemWriter<>();
        writer.setEntityManagerFactory(entityManagerFactory);
        
        log.info("Configured JpaItemWriter for Course entities");
        
        return writer;
    }
}
