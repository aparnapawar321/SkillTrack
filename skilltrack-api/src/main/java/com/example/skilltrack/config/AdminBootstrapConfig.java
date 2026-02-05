package com.example.skilltrack.config;

import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.repository.RoleRepository;
import com.example.skilltrack.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrapConfig implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:admin@skilltrack.com}")
    private String adminEmail;

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password:admin123}")
    private String adminPassword;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Checking for administrative user...");

        boolean adminRoleExists = userRepository.findAll().stream()
                .anyMatch(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getName() == Role.RoleName.ROLE_ADMIN));
        
        boolean adminUserExists = userRepository.existsByUsername(adminUsername);

        if (!adminRoleExists || !adminUserExists) {
            log.info("Required ADMIN setup missing. Bootstrapping administrator...");

            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.ROLE_ADMIN)));
            
            Role instructorRole = roleRepository.findByName(Role.RoleName.ROLE_INSTRUCTOR)
                    .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.ROLE_INSTRUCTOR)));
            
            Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                    .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.ROLE_STUDENT)));

            // Determine if the password should be encoded (if not already a BCrypt hash)
            String encodedPassword = adminPassword.startsWith("$2a$") || adminPassword.startsWith("$2b$") 
                ? adminPassword 
                : passwordEncoder.encode(adminPassword);

            User admin = User.builder()
                    .username(adminUsername)
                    .email(adminEmail)
                    .password(encodedPassword)
                    .enabled(true)
                    .deleted(false)
                    .build();

            admin.addRole(adminRole);
            admin.addRole(instructorRole);
            admin.addRole(studentRole);

            userRepository.save(admin);
            log.info("Default ADMIN user created successfully: {}", adminUsername);
        } else {
            log.info("ADMIN user already exists. Skipping bootstrap.");
        }
    }
}
