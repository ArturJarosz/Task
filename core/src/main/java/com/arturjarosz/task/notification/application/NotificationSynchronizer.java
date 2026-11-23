package com.arturjarosz.task.notification.application;

import com.arturjarosz.task.notification.domain.ItemRef;
import com.arturjarosz.task.notification.model.NotificationItemType;

import java.util.Set;

/**
 * Keeps notifications in step with the items they are about. Services saving such an item call it in the same
 * transaction, so a change shows at once; the items are checked right after the transaction commits, each in a
 * transaction of its own. The nightly job checks everything again.
 */
public interface NotificationSynchronizer {

    /**
     * Adds the notifications the item needs now, refreshes the ones it keeps and removes the ones that no longer apply,
     * read or not. A gone item loses all of them.
     */
    void synchronize(ItemRef item);

    default void synchronize(NotificationItemType itemType, Long itemId) {
        this.synchronize(new ItemRef(itemType, itemId));
    }

    /**
     * Synchronizes the project's items, after a change to the project or to any of them.
     */
    void synchronizeProject(Long projectId);

    /**
     * Synchronizes the items the architect decides the recipient of, after the architect's linked user changed.
     */
    void synchronizeArchitect(Long architectId);

    /**
     * Synchronizes the user's notifications of every item of given types that may need one, after the user changed
     * their settings. Other users' notifications are left as they are.
     */
    void synchronizeUserItemTypes(Long userId, Set<NotificationItemType> itemTypes);

    Set<ItemRef> getItemsToCheck();

    /**
     * Removes read notifications that were read long ago.
     */
    void removeOldReadNotifications();
}
