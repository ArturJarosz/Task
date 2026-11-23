package com.arturjarosz.task.notification.model;

import lombok.Getter;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.OptionalInt;

/**
 * Upcoming dates users are notified about, with the settings a user has until changing them.
 */
@Getter
public enum NotificationType {
    MEETING(NotificationItemType.CALENDAR_EVENT, true, 1),
    INSTALLMENT_PAYMENT(NotificationItemType.INSTALLMENT, true, 3),
    INSTALLMENT_OVERDUE(NotificationItemType.INSTALLMENT, true),
    CONTRACT_DEADLINE(NotificationItemType.CONTRACT, true, 7),
    STAGE_DEADLINE(NotificationItemType.STAGE, true, 3),
    COMPANY_COST_PAYMENT(NotificationItemType.COMPANY_COST, false, 3);

    public static final int MAX_DAYS_BEFORE = 30;
    public static final int MAX_REMINDERS = 5;

    private final NotificationItemType itemType;
    private final boolean enabledByDefault;
    private final List<Integer> defaultDaysBefore;

    NotificationType(NotificationItemType itemType, boolean enabledByDefault, Integer... defaultDaysBefore) {
        this.itemType = itemType;
        this.enabledByDefault = enabledByDefault;
        this.defaultDaysBefore = List.of(defaultDaysBefore);
    }

    /**
     * The type whose setting applies. An overdue installment is the same money as its payment, only late, so it has no
     * setting of its own.
     */
    public NotificationType getSettingType() {
        return this == INSTALLMENT_OVERDUE ? INSTALLMENT_PAYMENT : this;
    }

    public boolean isSetByUser() {
        return this.getSettingType() == this;
    }

    /**
     * The reminder an item due on given day is notified by today: of the reminders already reached, the one closest to
     * the due date; none before the first is reached. None is reached after the due date, so a user who turns a type on
     * isn't flooded with old ones; the ones already created stay while the item is open. A meeting is offered only
     * until it ends, so its reminders stay reached. An overdue installment has no reminders: it is notified once, while
     * it's late.
     */
    public OptionalInt reminderOn(LocalDate today, LocalDate dueDate, Collection<Integer> daysBefore) {
        if (this == INSTALLMENT_OVERDUE) {
            return OptionalInt.of(0);
        }
        if (this != MEETING && today.isAfter(dueDate)) {
            return OptionalInt.empty();
        }
        return daysBefore.stream().mapToInt(Integer::intValue)
                .filter(days -> !today.isBefore(dueDate.minusDays(days))).min();
    }
}
