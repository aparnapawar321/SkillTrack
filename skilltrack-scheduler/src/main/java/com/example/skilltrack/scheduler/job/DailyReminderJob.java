package com.example.skilltrack.scheduler.job;

import com.example.skilltrack.entity.Enrollment;
import com.example.skilltrack.repository.EnrollmentRepository;
import com.example.skilltrack.scheduler.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class DailyReminderJob extends QuartzJobBean {

    private final EnrollmentRepository enrollmentRepository;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        transactionTemplate.executeWithoutResult(status -> {
            log.info("Executing DailyReminderJob to send emails for incomplete courses...");
            
            List<Enrollment> incompleteEnrollments = enrollmentRepository.findIncompleteEnrollments();
            
            if (incompleteEnrollments.isEmpty()) {
                log.info("No incomplete courses found. Email sending skipped.");
                return;
            }

            for (Enrollment enrollment : incompleteEnrollments) {
                String toEmail = enrollment.getUser().getEmail();
                String courseTitle = enrollment.getCourse().getTitle();
                try {
                    emailService.sendReminderEmail(toEmail, courseTitle);
                } catch (Exception e) {
                    log.error("Failed to process reminder for user: {}", toEmail, e);
                }
            }
        });
        
        log.info("Finished executing DailyReminderJob.");
    }
}
