package com.example.skilltrack.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.envers.Audited;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Audited
@EntityListeners(AuditingEntityListener.class)
public class User {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false, length = 50)
    private String username;
    
    @Column(unique = true, nullable = false, length = 100)
    private String email;
    
    @Column(nullable = false)
    private String password;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean deleted = false;
    
    @Column(name = "roles_csv")
    private String rolesCsv;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_role_names", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role_name")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Set<Role.RoleName> roleNames = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();
    
    @Column(name = "oauth_provider")
    private String oauthProvider;  // e.g., "github"
    
    @Column(name = "oauth_id")
    private String oauthId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    // Helper methods
    public void addRole(Role role) {
        roles.add(role);
        roleNames.add(role.getName());
        updateRolesCsv();
        role.getUsers().add(this);
    }
    
    public void removeRole(Role role) {
        roles.remove(role);
        roleNames.remove(role.getName());
        updateRolesCsv();
        role.getUsers().remove(this);
    }

    public void updateRolesCsv() {
        this.rolesCsv = roleNames.stream()
                .map(Enum::name)
                .collect(java.util.stream.Collectors.joining(","));
    }
}
