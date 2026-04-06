package com.example.skilltrack.service;

import com.example.skilltrack.dto.JwtResponseDto;
import com.example.skilltrack.dto.LoginRequestDto;
import com.example.skilltrack.dto.UserDto;
import com.example.skilltrack.dto.UserRegistrationDto;
import com.example.skilltrack.exception.RegistrationException;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.repository.RoleRepository;
import com.example.skilltrack.repository.UserRepository;
import com.example.skilltrack.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final TransactionTemplate transactionTemplate;
    
    public UserDto registerUser(UserRegistrationDto registrationDto) {
        return transactionTemplate.execute(status -> {
            if (userRepository.existsByEmail(registrationDto.getEmail())) {
                throw new RegistrationException("Email already exists: " + registrationDto.getEmail());
            }
            
            if (userRepository.existsByUsername(registrationDto.getUsername())) {
                throw new RegistrationException("Username already exists: " + registrationDto.getUsername());
            }
            
            User user = User.builder()
                    .username(registrationDto.getUsername())
                    .email(registrationDto.getEmail())
                    .password(passwordEncoder.encode(registrationDto.getPassword()))
                    .enabled(true)
                    .deleted(false)
                    .build();
            
            Role studentRole = roleRepository.findByName(Role.RoleName.ROLE_STUDENT)
                    .orElseThrow(() -> new RuntimeException("Default role NOT_FOUND: ROLE_STUDENT"));
            user.addRole(studentRole);
            user.getRoleNames().add(Role.RoleName.ROLE_STUDENT);
            
            User savedUser = userRepository.save(user);
            log.info("New user registered as STUDENT: {}", savedUser.getUsername());
            
            return convertToDto(savedUser);
        });
    }
    
    public JwtResponseDto loginUser(LoginRequestDto loginRequest) {
        return transactionTemplate.execute(status -> {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getEmailOrUsername(),
                            loginRequest.getPassword()
                    )
            );
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            String jwt = tokenProvider.generateTokenWithRoles(
                    authentication.getName(),
                    authentication.getAuthorities()
            );
            String refreshToken = tokenProvider.generateToken(authentication.getName(), true);
            
            User user = userRepository.findActiveByEmail(loginRequest.getEmailOrUsername())
                    .or(() -> userRepository.findActiveByUsername(loginRequest.getEmailOrUsername()))
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Set<String> roles = authentication.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            
            return JwtResponseDto.builder()
                    .token(jwt)
                    .refreshToken(refreshToken)
                    .userId(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .roles(roles)
                    .build();
        });
    }
    
    @Cacheable(value = "users", key = "T(org.springframework.security.core.context.SecurityContextHolder).getContext().getAuthentication().getName()")
    public UserDto getCurrentUser() {
        return transactionTemplate.execute(status -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            
            User user = userRepository.findActiveByEmail(username)
                    .or(() -> userRepository.findActiveByUsername(username))
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            return convertToDto(user);
        });
    }
    
    @Cacheable(value = "users", key = "#id")
    public UserDto getUserById(Long id) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            return convertToDto(user);
        });
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public UserDto updateUserRoles(Long userId, Set<Role.RoleName> roleNames) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String currentUsername = authentication.getName();
            
            if (user.getUsername().equals(currentUsername)) {
                boolean removingAdmin = user.getRoles().stream()
                        .anyMatch(r -> r.getName() == Role.RoleName.ROLE_ADMIN) &&
                        !roleNames.contains(Role.RoleName.ROLE_ADMIN);
                
                if (removingAdmin) {
                    throw new RuntimeException("Security violation: You cannot remove the ADMIN role from yourself.");
                }
            }
            
            user.getRoles().clear();
            user.getRoleNames().clear();
            
            for (Role.RoleName roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                user.addRole(role);
            }
            
            User updatedUser = userRepository.save(user);
            log.info("Updated roles for user {}: {}", updatedUser.getUsername(), roleNames);
            
            return convertToDto(updatedUser);
        });
    }
    
    @CacheEvict(value = "users", allEntries = true)
    public UserDto addRoleToUser(Long userId, Role.RoleName roleName) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
            
            user.addRole(role);
            User updatedUser = userRepository.save(user);
            log.info("Added role {} to user {}", roleName, updatedUser.getUsername());
            return convertToDto(updatedUser);
        });
    }

    @CacheEvict(value = "users", allEntries = true)
    public UserDto removeRoleFromUser(Long userId, Role.RoleName roleName) {
        return transactionTemplate.execute(status -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (user.getUsername().equals(authentication.getName()) && roleName == Role.RoleName.ROLE_ADMIN) {
                throw new RuntimeException("Security violation: You cannot remove the ADMIN role from yourself.");
            }

            user.getRoles().removeIf(role -> {
                if (role.getName() == roleName) {
                    user.getRoleNames().remove(roleName);
                    return true;
                }
                return false;
            });
            user.updateRolesCsv();
            User updatedUser = userRepository.save(user);
            log.info("Removed role {} from user {}", roleName, updatedUser.getUsername());
            return convertToDto(updatedUser);
        });
    }

    @CacheEvict(value = "users", allEntries = true)
    public UserDto setUserMainRole(Long userId, Role.RoleName targetRole) {
        return transactionTemplate.execute(status -> {
            if (targetRole != Role.RoleName.ROLE_STUDENT && targetRole != Role.RoleName.ROLE_INSTRUCTOR) {
                throw new RuntimeException("This API only supports STUDENT or INSTRUCTOR assignments.");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.getRoles().removeIf(role -> 
                role.getName() == Role.RoleName.ROLE_STUDENT || 
                role.getName() == Role.RoleName.ROLE_INSTRUCTOR
            );
            user.getRoleNames().remove(Role.RoleName.ROLE_STUDENT);
            user.getRoleNames().remove(Role.RoleName.ROLE_INSTRUCTOR);

            Role role = roleRepository.findByName(targetRole)
                    .orElseThrow(() -> new RuntimeException("Role not found: " + targetRole));
            user.addRole(role);

            User updatedUser = userRepository.save(user);
            log.info("Set main role for user {} to {}", updatedUser.getUsername(), targetRole);
            return convertToDto(updatedUser);
        });
    }
    
    private UserDto convertToDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .roles(user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.toSet()))
                .oauthProvider(user.getOauthProvider())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
