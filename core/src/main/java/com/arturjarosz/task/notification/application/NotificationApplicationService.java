package com.arturjarosz.task.notification.application;

import com.arturjarosz.task.dto.NotificationCountDto;
import com.arturjarosz.task.dto.NotificationReadDto;
import com.arturjarosz.task.dto.NotificationTypeDto;
import com.arturjarosz.task.dto.NotificationsDto;

/**
 * Notifications of the current user.
 */
public interface NotificationApplicationService {

    NotificationsDto getNotifications(Boolean unread, NotificationTypeDto type, Integer page, Integer size);

    NotificationCountDto getNotificationCount();

    void updateNotificationRead(Long notificationId, NotificationReadDto notificationReadDto);

    void markAllNotificationsRead();
}
