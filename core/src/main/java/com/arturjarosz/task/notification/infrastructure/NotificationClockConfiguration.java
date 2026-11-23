package com.arturjarosz.task.notification.infrastructure;

import com.arturjarosz.task.notification.domain.NotificationTime;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class NotificationClockConfiguration {

    @Bean
    public Clock notificationClock() {
        return Clock.system(NotificationTime.ZONE);
    }
}
