package com.arturjarosz.task.notification.query;

import com.arturjarosz.task.notification.domain.ItemDueDates;
import com.arturjarosz.task.notification.domain.ItemRef;
import com.arturjarosz.task.notification.model.NotificationItemType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Reads the items of other modules that users are notified about.
 */
public interface DueItemQueryService {

    /**
     * What the item is due for now. Unpaid company cost charges are read from {@code since}, so ones already notified
     * about can be kept while they stay unpaid.
     */
    ItemDueDates getDueDates(ItemRef item, LocalDate today, LocalDateTime now, LocalDate since);

    /**
     * Items of given types that may need a notification today: due within the longest notice period, overdue unpaid
     * installments, and every item that already has a notification, so it can be refreshed or removed.
     */
    Set<ItemRef> getItemsToCheck(Set<NotificationItemType> itemTypes, LocalDate today, LocalDateTime now);

    /**
     * The project's contract, stages, installments and meetings, and the items its notifications are about, so the ones
     * of items removed with the project or moved out of it are found too.
     */
    Set<ItemRef> getItemsOfProject(long projectId);

    /**
     * Items whose recipient is decided by the architect: its projects with their items, and its own stages and meetings.
     */
    Set<ItemRef> getItemsOfArchitect(long architectId);
}
