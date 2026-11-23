package com.arturjarosz.task.notification.model;

import com.arturjarosz.task.notification.domain.DueItem;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;

import java.io.Serial;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Tells a user about one item due on one day. The item and project names are copies, so the list needs no joins; they
 * are refreshed whenever the item is checked again. Each reminder the user set for its type makes it unread again.
 */
@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "notification_sequence", allocationSize = 1)
@Table(name = "NOTIFICATION")
@Getter
public class Notification extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 4630184219947702136L;

    @Column(name = "USER_ID", nullable = false, updatable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, updatable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "ITEM_TYPE", nullable = false, updatable = false)
    private NotificationItemType itemType;

    @Column(name = "ITEM_ID", nullable = false, updatable = false)
    private Long itemId;

    @Column(name = "PROJECT_ID")
    private Long projectId;

    @Column(name = "STAGE_ID")
    private Long stageId;

    @Column(name = "DUE_DATE", nullable = false, updatable = false)
    private LocalDate dueDate;

    @Column(name = "DUE_DATETIME")
    private LocalDateTime dueDateTime;

    @Column(name = "ITEM_NAME")
    private String itemName;

    @Column(name = "PROJECT_NAME")
    private String projectName;

    @Column(name = "READ_DATETIME")
    private LocalDateTime readDateTime;

    // Days before the due date of the reminder that made it unread last
    @Column(name = "REMINDED_DAYS_BEFORE", nullable = false)
    private int remindedDaysBefore;

    protected Notification() {
        // needed by JPA
    }

    public Notification(Long userId, DueItem dueItem, int remindedDaysBefore) {
        this.userId = userId;
        this.type = dueItem.type();
        this.itemType = dueItem.itemType();
        this.itemId = dueItem.itemId();
        this.dueDate = dueItem.dueDate();
        this.remindedDaysBefore = remindedDaysBefore;
        this.refresh(dueItem);
    }

    public void refresh(DueItem dueItem) {
        this.projectId = dueItem.projectId();
        this.stageId = dueItem.stageId();
        this.dueDateTime = dueItem.dueDateTime();
        this.itemName = dueItem.itemName();
        this.projectName = dueItem.projectName();
    }

    /**
     * Makes the notification unread again when a reminder closer to the due date than the last one is reached.
     */
    public void remind(int daysBefore) {
        if (daysBefore < this.remindedDaysBefore) {
            this.remindedDaysBefore = daysBefore;
            this.markUnread();
        }
    }

    public boolean isRead() {
        return this.readDateTime != null;
    }

    public void markRead(LocalDateTime now) {
        if (!this.isRead()) {
            this.readDateTime = now;
        }
    }

    public void markUnread() {
        this.readDateTime = null;
    }
}
