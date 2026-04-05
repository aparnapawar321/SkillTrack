package com.example.skilltrack.scheduler.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        // Set up @Value for enabled flag
        ReflectionTestUtils.setField(emailService, "emailEnabled", true);
    }

    @Test
    void sendReminderEmail_WhenEnabled_SendsEmail() {
        emailService.sendReminderEmail("student@example.com", "Test Course");

        verify(mailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendReminderEmail_WhenDisabled_DoesNotSendEmail() {
        ReflectionTestUtils.setField(emailService, "emailEnabled", false);

        emailService.sendReminderEmail("student@example.com", "Test Course");

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }
}
