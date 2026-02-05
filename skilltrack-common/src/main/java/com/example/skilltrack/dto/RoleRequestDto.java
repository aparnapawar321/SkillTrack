package com.example.skilltrack.dto;

import com.example.skilltrack.entity.Role;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequestDto {
    @NotNull(message = "Role name is required")
    private Role.RoleName roleName;
}
