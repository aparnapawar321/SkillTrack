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

        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.ROLE_ADMIN)));
        
        Role instructorRole = roleRepository.findByName(Role.RoleName.ROLE_INSTRUCTOR)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.ROLE_INSTRUCTOR)));
        
        Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                .orElseGet(() -> roleRepository.save(new Role(Role.RoleName.ROLE_STUDENT)));

        User admin = userRepository.findByUsername(adminUsername)
                .orElseGet(() -> User.builder()
                        .username(adminUsername)
                        .email(adminEmail)
                        .enabled(true)
                        .deleted(false)
                        .build());

        // Always update password and roles to match configuration
        String encodedPassword = adminPassword.startsWith("$2a$") || adminPassword.startsWith("$2b$") 
            ? adminPassword 
            : passwordEncoder.encode(adminPassword);
        
        admin.setPassword(encodedPassword);
        admin.setEmail(adminEmail); // Ensure email is correct too

        // Clear and re-add roles to ensure consistency
        admin.getRoles().clear();
        admin.getRoleNames().clear();
        
        admin.addRole(adminRole);
        admin.addRole(instructorRole);
        admin.addRole(studentRole);

        userRepository.save(admin);
        log.info("ADMIN user synchronized successfully: {}", adminUsername);
    }
}
