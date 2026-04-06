package com.example.skilltrack.service;

import com.example.skilltrack.dto.UserDto;
import com.example.skilltrack.entity.Role;
import com.example.skilltrack.entity.User;
import com.example.skilltrack.repository.RoleRepository;
import com.example.skilltrack.repository.UserRepository;
import com.example.skilltrack.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private TransactionTemplate transactionTemplate;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .enabled(true)
                .build();

        // Simple mock to execute the callback for TransactionTemplate
        lenient().when(transactionTemplate.execute(any())).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(null);
        });

        lenient().doAnswer(invocation -> {
            java.util.function.Consumer<org.springframework.transaction.TransactionStatus> callback = invocation.getArgument(0);
            callback.accept(null);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    void getUserById_WhenExists_ReturnsDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        UserDto result = userService.getUserById(1L);

        assertNotNull(result);
        assertEquals("testuser", result.getUsername());
        verify(userRepository).findById(1L);
    }
}
