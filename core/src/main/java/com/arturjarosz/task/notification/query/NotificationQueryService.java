package com.arturjarosz.task.notification.query;

import com.arturjarosz.task.notification.model.Notification;
import com.arturjarosz.task.notification.model.NotificationType;

import java.util.List;

public interface NotificationQueryService {

    /**
     * A page of the user's notifications, the earliest due first, so overdue ones come before upcoming ones.
     */
    List<Notification> getNotifications(long userId, boolean unreadOnly, NotificationType type, int page, int size);

    long countNotifications(long userId, boolean unreadOnly, NotificationType type);
}
