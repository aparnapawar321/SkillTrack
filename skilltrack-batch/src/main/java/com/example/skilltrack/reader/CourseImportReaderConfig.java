package com.example.skilltrack.reader;

import com.example.skilltrack.dto.CourseImportDTO;
import com.example.skilltrack.dto.CourseModuleImportDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.validation.BindException;

import java.util.ArrayList;
import java.util.List;

/**
 * Configuration for reading course data from CSV files.
 * Maps CSV columns to CourseImportDTO objects with support for nested modules.
 */
@Slf4j
@Configuration
public class CourseImportReaderConfig {
    
    @Value("${app.batch.import.directory:./data/import}")
    private String importDirectory;
    
    /**
     * Creates a FlatFileItemReader for reading course CSV files with modules.
     * 
     * Expected CSV format:
     * title,description,instructorId,instructorEmail,modules
     * 
     * Modules format (pipe-separated):
     * "Module1Title:Module1Content:0|Module2Title:Module2Content:1"
     * 
     * @param filename the CSV file to read
     * @return configured FlatFileItemReader
     */
    @Bean
    public FlatFileItemReader<CourseImportDTO> courseImportReader(
            @Value("${course.import.filename:courses.csv}") String filename) {
        
        FlatFileItemReader<CourseImportDTO> reader = new FlatFileItemReader<>();
        reader.setName("courseImportReader");
        reader.setResource(new FileSystemResource(importDirectory + "/" + filename));
        reader.setLinesToSkip(1); // Skip header row
        
        // Configure custom line mapper
        reader.setLineMapper(createLineMapper());
        
        log.info("Configured CourseImportReader for file: {}/{}", importDirectory, filename);
        
        return reader;
    }
    
    /**
     * Creates a custom line mapper that handles module parsing.
     */
    private LineMapper<CourseImportDTO> createLineMapper() {
        return (line, lineNumber) -> {
            DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
            tokenizer.setDelimiter(",");
            tokenizer.setNames("title", "description", "instructorId", "instructorEmail", "modules");
            tokenizer.setStrict(false);
            
            FieldSet fieldSet = tokenizer.tokenize(line);
            
            CourseImportDTO dto = new CourseImportDTO();
            dto.setTitle(fieldSet.readString("title"));
            dto.setDescription(fieldSet.readString("description"));
            
            // Handle instructorId (may be empty)
            String instructorIdStr = fieldSet.readString("instructorId");
            if (instructorIdStr != null && !instructorIdStr.trim().isEmpty()) {
                dto.setInstructorId(Long.parseLong(instructorIdStr.trim()));
            }
            
            dto.setInstructorEmail(fieldSet.readString("instructorEmail"));
            dto.setRowNumber(lineNumber);
            dto.setRawData(line);
            
            // Parse modules
            String modulesStr = fieldSet.readString("modules");
            if (modulesStr != null && !modulesStr.trim().isEmpty()) {
                dto.setModules(parseModules(modulesStr));
            }
            
            return dto;
        };
    }
    
    /**
     * Parses module data from pipe-separated format.
     * Format: "Title:Content:OrderIndex|Title:Content:OrderIndex"
     * 
     * @param modulesStr the modules string
     * @return list of CourseModuleImportDTO
     */
    private List<CourseModuleImportDTO> parseModules(String modulesStr) {
        List<CourseModuleImportDTO> modules = new ArrayList<>();
        
        if (modulesStr == null || modulesStr.trim().isEmpty()) {
            return modules;
        }
        
        // Split by pipe to get individual modules
        String[] moduleEntries = modulesStr.split("\\|");
        
        for (String moduleEntry : moduleEntries) {
            if (moduleEntry.trim().isEmpty()) {
                continue;
            }
            
            // Split by colon to get module fields
            String[] parts = moduleEntry.split(":", 3); // Limit to 3 parts in case content has colons
            
            if (parts.length >= 3) {
                CourseModuleImportDTO module = CourseModuleImportDTO.builder()
                        .title(parts[0].trim())
                        .content(parts[1].trim())
                        .orderIndex(Integer.parseInt(parts[2].trim()))
                        .build();
                modules.add(module);
            }
        }
        
        return modules;
    }
}
