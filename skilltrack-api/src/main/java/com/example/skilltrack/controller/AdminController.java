package com.example.skilltrack.controller;

import com.example.skilltrack.dto.CourseDto;
import com.example.skilltrack.dto.RoleRequestDto;
import com.example.skilltrack.dto.UserDto;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.service.CourseService;
import com.example.skilltrack.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Tag(name = "Admin", description = "Administrative operations for user and role management")
@SecurityRequirement(name = "bearerAuth")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final CourseService courseService;

    @PutMapping("/users/{userId}/roles")
    @Operation(summary = "Update user roles", description = "Updates the roles assigned to a user (Admin only)")
    public ResponseEntity<UserDto> updateUserRoles(
            @PathVariable Long userId,
            @RequestBody Set<Role.RoleName> roleNames) {
        UserDto updatedUser = userService.updateUserRoles(userId, roleNames);
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping("/users/{userId}")
    @Operation(summary = "Get any user details", description = "Retrieves details of any user by ID (Admin only)")
    public ResponseEntity<UserDto> getUserById(@PathVariable Long userId) {
        UserDto user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    @PostMapping("/users/{userId}/roles")
    @Operation(summary = "Add role to user", description = "Adds a specific role to a user (Admin only)")
    public ResponseEntity<UserDto> addRoleToUser(
            @PathVariable Long userId,
            @RequestBody RoleRequestDto roleRequest) {
        UserDto updatedUser = userService.addRoleToUser(userId, roleRequest.getRoleName());
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{userId}/roles")
    @Operation(summary = "Remove role from user", description = "Removes a specific role from a user (Admin only)")
    public ResponseEntity<UserDto> removeRoleFromUser(
            @PathVariable Long userId,
            @RequestBody RoleRequestDto roleRequest) {
        UserDto updatedUser = userService.removeRoleFromUser(userId, roleRequest.getRoleName());
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/users/{userId}/main-role")
    @Operation(summary = "Update user main role", description = "Specifically switch user between STUDENT and INSTRUCTOR roles (Admin only)")
    public ResponseEntity<UserDto> setUserMainRole(
            @PathVariable Long userId,
            @RequestBody RoleRequestDto roleRequest) {
        UserDto updatedUser = userService.setUserMainRole(userId, roleRequest.getRoleName());
        return ResponseEntity.ok(updatedUser);
    }

    @PostMapping("/courses/{courseId}/restore")
    @Operation(summary = "Restore a soft-deleted course", description = "Restores a course that was soft-deleted (Admin only)")
    public ResponseEntity<CourseDto> restoreCourse(@PathVariable Long courseId) {
        CourseDto restoredCourse = courseService.restoreCourse(courseId);
        return ResponseEntity.ok(restoredCourse);
    }
}
