package com.arturjarosz.task.notification.infrastructure.schedule;

import com.arturjarosz.task.notification.application.NotificationSynchronizer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Checks every item that may need a notification once a night, when the dates move into or out of their notice
 * periods, and once on start, so a night the server was down isn't missed. Each item is checked in its own
 * transaction, so one that fails doesn't stop the others; the synchronizer logs the ones that fail.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class NotificationJob {

    private final NotificationSynchronizer notificationSynchronizer;

    @EventListener(ApplicationReadyEvent.class)
    public void onStart() {
        this.synchronizeAll();
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "Europe/Warsaw")
    public void synchronizeAll() {
        LOG.info("Checking notifications of all items.");
        try {
            var items = this.notificationSynchronizer.getItemsToCheck();
            items.forEach(this.notificationSynchronizer::synchronize);
            this.notificationSynchronizer.removeOldReadNotifications();
            LOG.info("Notifications of {} items checked.", items.size());
        } catch (Exception e) {
            LOG.error("Checking notifications failed.", e);
        }
    }
}
