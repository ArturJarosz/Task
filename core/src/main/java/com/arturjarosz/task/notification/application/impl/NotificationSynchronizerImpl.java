package com.arturjarosz.task.notification.application.impl;

import com.arturjarosz.task.notification.application.NotificationPreferences;
import com.arturjarosz.task.notification.application.NotificationRecipientResolver;
import com.arturjarosz.task.notification.application.NotificationSynchronizer;
import com.arturjarosz.task.notification.domain.ItemRef;
import com.arturjarosz.task.notification.infrastructure.repository.NotificationRepository;
import com.arturjarosz.task.notification.model.Notification;
import com.arturjarosz.task.notification.model.NotificationItemType;
import com.arturjarosz.task.notification.model.NotificationType;
import com.arturjarosz.task.notification.query.DueItemQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class NotificationSynchronizerImpl implements NotificationSynchronizer {

    private static final int KEEP_READ_DAYS = 90;

    private final Clock clock;
    private final DueItemQueryService dueItemQueryService;
    private final NotificationPreferences notificationPreferences;
    private final NotificationRecipientResolver notificationRecipientResolver;
    private final NotificationRepository notificationRepository;
    private final PlatformTransactionManager transactionManager;

    @Transactional
    @Override
    public void synchronize(ItemRef item) {
        this.whenChangesAreDone(pending -> pending.items.add(item));
    }

    @Transactional
    @Override
    public void synchronizeProject(Long projectId) {
        this.whenChangesAreDone(pending -> pending.projectIds.add(projectId));
    }

    @Transactional
    @Override
    public void synchronizeArchitect(Long architectId) {
        this.whenChangesAreDone(pending -> pending.architectIds.add(architectId));
    }

    /**
     * A save can change an item more than once and remove parts of it on the way (a stage takes its tasks and
     * installment with it), so items are checked once, right after the transaction commits, when every change is
     * done. Each item is checked in a transaction of its own, so a notification that fails to save never undoes the
     * user's change; the nightly job puts it right. Outside a transaction they are checked at once.
     */
    private void whenChangesAreDone(Consumer<PendingSynchronization> request) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            var pending = new PendingSynchronization();
            request.accept(pending);
            pending.itemsToCheck().forEach(this::synchronizeNow);
            return;
        }
        var pending = (PendingSynchronization) TransactionSynchronizationManager.getResource(this);
        if (pending == null) {
            pending = new PendingSynchronization();
            TransactionSynchronizationManager.bindResource(this, pending);
            TransactionSynchronizationManager.registerSynchronization(pending);
        }
        request.accept(pending);
    }

    private void synchronizeNow(ItemToCheck item) {
        this.synchronizeNow(item.item(), item.forUser());
    }

    /**
     * Checks the item's notifications of the users given. The other users' notifications are left as they are.
     */
    private void synchronizeNow(ItemRef item, Predicate<Long> forUser) {
        var today = LocalDate.now(this.clock);
        var existing = this.notificationRepository.findByItemTypeAndItemId(item.itemType(), item.itemId()).stream()
                .filter(notification -> forUser.test(notification.getUserId())).toList();
        // Charges already notified about are kept while unpaid, so they are read from the earliest one
        var since = existing.stream().map(Notification::getDueDate).filter(today::isAfter).min(LocalDate::compareTo)
                .orElse(today);
        var dueDates = this.dueItemQueryService.getDueDates(item, today, LocalDateTime.now(this.clock), since);
        if (dueDates.gone()) {
            this.notificationRepository.deleteAll(existing);
            return;
        }
        var kept = new HashSet<Notification>();
        for (var dueItem : dueDates.dueItems()) {
            var recipients = this.notificationRecipientResolver.resolve(dueItem).stream().filter(forUser).toList();
            var preferences = this.notificationPreferences.get(recipients, dueItem.type());
            for (var userId : recipients) {
                var preference = preferences.get(userId);
                if (!preference.enabled()) {
                    continue;
                }
                var reminder = dueItem.type().reminderOn(today, dueItem.dueDate(), preference.daysBefore());
                var match = existing.stream().filter(notification -> notification.getUserId().equals(userId)
                        && notification.getType() == dueItem.type()
                        && notification.getDueDate().equals(dueItem.dueDate())).findFirst();
                if (match.isPresent()) {
                    reminder.ifPresent(match.get()::remind);
                    match.get().refresh(dueItem);
                    kept.add(match.get());
                } else if (reminder.isPresent()) {
                    this.notificationRepository.save(new Notification(userId, dueItem, reminder.getAsInt()));
                }
            }
        }
        // A notification lives only while it's true: one about a paid installment or an old date, read or not, is gone
        this.notificationRepository.deleteAll(existing.stream()
                .filter(notification -> !kept.contains(notification)).toList());
    }

    @Transactional
    @Override
    public void synchronizeUserItemTypes(Long userId, Set<NotificationItemType> itemTypes) {
        this.whenChangesAreDone(pending -> pending.userItemTypes.computeIfAbsent(userId,
                key -> EnumSet.noneOf(NotificationItemType.class)).addAll(itemTypes));
    }

    @Transactional(readOnly = true)
    @Override
    public Set<ItemRef> getItemsToCheck() {
        return this.dueItemQueryService.getItemsToCheck(EnumSet.allOf(NotificationItemType.class),
                LocalDate.now(this.clock), LocalDateTime.now(this.clock));
    }

    @Transactional
    @Override
    public void removeOldReadNotifications() {
        // An overdue installment would be notified again the next night, unread, so it stays until it's paid
        var removed = this.notificationRepository.deleteReadBefore(
                LocalDateTime.now(this.clock).minusDays(KEEP_READ_DAYS), NotificationType.INSTALLMENT_OVERDUE);
        LOG.info("Removed {} notifications read more than {} days ago.", removed, KEEP_READ_DAYS);
    }

    private TransactionTemplate newTransaction(boolean readOnly) {
        var template = new TransactionTemplate(this.transactionManager);
        // The committed transaction's resources are still bound after commit, so only a new one writes
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        template.setReadOnly(readOnly);
        return template;
    }

    /**
     * An item to check, for every recipient or only for the users whose settings changed.
     */
    private record ItemToCheck(ItemRef item, Predicate<Long> forUser) {
    }

    private final class PendingSynchronization implements TransactionSynchronization {
        private final Set<ItemRef> items = new LinkedHashSet<>();
        private final Set<Long> projectIds = new LinkedHashSet<>();
        private final Set<Long> architectIds = new LinkedHashSet<>();
        private final Map<Long, Set<NotificationItemType>> userItemTypes = new LinkedHashMap<>();

        private Set<ItemToCheck> itemsToCheck() {
            var synchronizer = NotificationSynchronizerImpl.this;
            var queryService = synchronizer.dueItemQueryService;
            var allItems = new LinkedHashSet<>(this.items);
            this.projectIds.forEach(projectId -> allItems.addAll(queryService.getItemsOfProject(projectId)));
            this.architectIds.forEach(architectId -> allItems.addAll(queryService.getItemsOfArchitect(architectId)));
            var toCheck = new LinkedHashSet<ItemToCheck>();
            allItems.forEach(item -> toCheck.add(new ItemToCheck(item, userId -> true)));
            this.userItemTypes.forEach((userId, itemTypes) -> queryService.getItemsToCheck(itemTypes,
                            LocalDate.now(synchronizer.clock), LocalDateTime.now(synchronizer.clock))
                    .forEach(item -> toCheck.add(new ItemToCheck(item, userId::equals))));
            return toCheck;
        }

        @Override
        public void afterCommit() {
            var synchronizer = NotificationSynchronizerImpl.this;
            Set<ItemToCheck> toCheck;
            try {
                toCheck = synchronizer.newTransaction(true).execute(status -> this.itemsToCheck());
            } catch (RuntimeException e) {
                LOG.warn("Finding the items to check notifications of failed.", e);
                return;
            }
            if (toCheck == null) {
                return;
            }
            var writing = synchronizer.newTransaction(false);
            for (var item : toCheck) {
                try {
                    writing.executeWithoutResult(status -> synchronizer.synchronizeNow(item));
                } catch (RuntimeException e) {
                    LOG.warn("Checking notifications of {} {} failed.", item.item().itemType(), item.item().itemId(),
                            e);
                }
            }
        }

        @Override
        public void afterCompletion(int status) {
            TransactionSynchronizationManager.unbindResourceIfPossible(NotificationSynchronizerImpl.this);
        }
    }
}
