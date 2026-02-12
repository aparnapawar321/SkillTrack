package com.example.skilltrack.exception;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * Custom exception for validation errors during batch import.
 * Contains detailed validation error messages.
 */
@Getter
public class BatchValidationException extends Exception {
    
    private final List<String> errors;
    private final String rawData;
    private final Integer rowNumber;
    
    public BatchValidationException(String message) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(message);
        this.rawData = null;
        this.rowNumber = null;
    }
    
    public BatchValidationException(List<String> errors, String rawData, Integer rowNumber) {
        super(String.join("; ", errors));
        this.errors = errors;
        this.rawData = rawData;
        this.rowNumber = rowNumber;
    }
    
    public BatchValidationException(String message, String rawData, Integer rowNumber) {
        super(message);
        this.errors = new ArrayList<>();
        this.errors.add(message);
        this.rawData = rawData;
        this.rowNumber = rowNumber;
    }
    
    public String getFormattedErrors() {
        return String.join("; ", errors);
    }
}
