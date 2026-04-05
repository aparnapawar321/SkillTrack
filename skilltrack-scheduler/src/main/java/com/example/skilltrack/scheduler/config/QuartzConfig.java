package com.example.skilltrack.scheduler.config;

import com.example.skilltrack.scheduler.job.DailyReminderJob;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzConfig {

    public static final String DAILY_REMINDER_JOB_KEY = "reminderEmailJob";

    @Bean
    public JobDetail dailyReminderJobDetail() {
        return JobBuilder.newJob(DailyReminderJob.class)
                .withIdentity(DAILY_REMINDER_JOB_KEY)
                .withDescription("Daily job to send email reminders for incomplete courses")
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger dailyReminderJobTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(dailyReminderJobDetail())
                .withIdentity("DailyReminderJobTrigger")
                .withDescription("Trigger for DailyReminderJob")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 8 * * ?")) // 8:00 AM every day
                .build();
    }
}
