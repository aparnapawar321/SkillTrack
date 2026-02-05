package com.example.skilltrack.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer {
    
    private final RoleRepository roleRepository;
    
    @Bean
    public CommandLineRunner initializeRoles() {
        return args -> {
            // Initialize default roles if they don't exist
            for (Role.RoleName roleName : Role.RoleName.values()) {
                if (roleRepository.findByName(roleName).isEmpty()) {
                    Role role = Role.builder()
                            .name(roleName)
                            .build();
                    roleRepository.save(role);
                    log.info("Created role: {}", roleName);
                }
            }
        };
    }
}
