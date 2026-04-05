package com.example.skilltrack.scheduler.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender javaMailSender;

    @org.springframework.beans.factory.annotation.Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public void sendReminderEmail(String toEmail, String courseTitle) {
        if (!emailEnabled) {
            log.info("Email sending is disabled. Skipping reminder for {}", toEmail);
            return;
        }
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("Reminder: Complete Your Course!");
            message.setText("Hello,\n\nThis is a friendly reminder to complete your enrolled course: " + courseTitle + ".\n\nKeep up the great work!\n\nThe SkillTrack Team");
            
            javaMailSender.send(message);
            log.info("Reminder email sent to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send reminder email to {}: {}", toEmail, e.getMessage());
        }
    }
}
