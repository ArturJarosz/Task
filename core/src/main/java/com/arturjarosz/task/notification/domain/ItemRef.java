package com.arturjarosz.task.notification.domain;

import com.arturjarosz.task.notification.model.NotificationItemType;

/**
 * An item that can have notifications.
 */
public record ItemRef(NotificationItemType itemType, long itemId) {
}
