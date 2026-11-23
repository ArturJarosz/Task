package com.arturjarosz.task.notification.domain;

import com.arturjarosz.task.notification.model.NotificationItemType;
import com.arturjarosz.task.notification.model.NotificationType;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * An item due on a day, which its recipients are notified about. The recipient is the user linked to the architect, or
 * every user when the item is for everyone; an architect without a linked user means no one.
 */
public record DueItem(NotificationType type, NotificationItemType itemType, long itemId, Long projectId, Long stageId,
                      LocalDate dueDate, LocalDateTime dueDateTime, String itemName, String projectName,
                      Long architectId, boolean forEveryone) {
}
