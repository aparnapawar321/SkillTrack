package com.example.skilltrack.controller;

import com.example.skilltrack.dto.JwtResponseDto;
import com.example.skilltrack.dto.LoginRequestDto;
import com.example.skilltrack.dto.UserDto;
import com.example.skilltrack.dto.UserRegistrationDto;
import com.example.skilltrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and registration endpoints")
public class AuthController {
    
    private final UserService userService;
    
    @PostMapping("/register")
    @Operation(summary = "Register a new user", description = "Creates a new user account. Roles can be specified (ADMIN, INSTRUCTOR, STUDENT). Defaults to STUDENT if not provided.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input or user already exists")
    })
    public ResponseEntity<UserDto> registerUser(@Valid @RequestBody UserRegistrationDto registrationDto) {
        UserDto userDto = userService.registerUser(registrationDto);
        return new ResponseEntity<>(userDto, HttpStatus.CREATED);
    }
    
    @PostMapping("/login")
    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<JwtResponseDto> loginUser(@Valid @RequestBody LoginRequestDto loginRequest) {
        JwtResponseDto jwtResponse = userService.loginUser(loginRequest);
        return ResponseEntity.ok(jwtResponse);
    }
    
    @GetMapping("/me")
    @Operation(summary = "Get current user", description = "Returns the currently authenticated user's profile")
    @ApiResponse(responseCode = "200", description = "User profile retrieved successfully")
    public ResponseEntity<UserDto> getCurrentUser() {
        UserDto userDto = userService.getCurrentUser();
        return ResponseEntity.ok(userDto);
    }
}
