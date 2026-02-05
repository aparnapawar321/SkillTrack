package com.example.skilltrack.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private Long id;
    private String username;
    private String email;
    private Boolean enabled;
    private Set<String> roles;
    private String oauthProvider;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
