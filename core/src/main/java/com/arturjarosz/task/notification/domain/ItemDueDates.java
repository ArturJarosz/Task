package com.arturjarosz.task.notification.domain;

import java.util.List;

/**
 * What an item is due for now. A gone item (deleted, or a meeting that has ended) loses all its notifications, read
 * ones too, as there is nothing left to open; an open item keeps its read notifications.
 */
public record ItemDueDates(boolean gone, List<DueItem> dueItems) {

    public static ItemDueDates itemGone() {
        return new ItemDueDates(true, List.of());
    }

    public static ItemDueDates of(List<DueItem> dueItems) {
        return new ItemDueDates(false, dueItems);
    }
}
