package com.arturjarosz.task.notification.application.impl

import com.arturjarosz.task.notification.application.NotificationPreferences
import com.arturjarosz.task.notification.application.NotificationRecipientResolver
import com.arturjarosz.task.notification.domain.DueItem
import com.arturjarosz.task.notification.domain.ItemDueDates
import com.arturjarosz.task.notification.domain.ItemRef
import com.arturjarosz.task.notification.domain.NotificationPreference
import com.arturjarosz.task.notification.domain.NotificationTime
import com.arturjarosz.task.notification.infrastructure.repository.NotificationRepository
import com.arturjarosz.task.notification.model.Notification
import com.arturjarosz.task.notification.model.NotificationItemType
import com.arturjarosz.task.notification.model.NotificationType
import com.arturjarosz.task.notification.query.DueItemQueryService
import org.springframework.transaction.PlatformTransactionManager
import spock.lang.Specification

import java.time.Clock
import java.time.LocalDate
import java.time.LocalDateTime

class NotificationSynchronizerImplTest extends Specification {

    static final TODAY = LocalDate.of(2026, 11, 15)
    static final STAGE_ID = 3L
    static final USER_ID = 7L
    static final OTHER_USER_ID = 8L
    static final STAGE = new ItemRef(NotificationItemType.STAGE, STAGE_ID)

    def clock = Clock.fixed(TODAY.atTime(9, 0).atZone(NotificationTime.ZONE).toInstant(), NotificationTime.ZONE)
    def dueItemQueryService = Mock(DueItemQueryService)
    def notificationPreferences = Mock(NotificationPreferences)
    def notificationRecipientResolver = Mock(NotificationRecipientResolver)
    def notificationRepository = Mock(NotificationRepository)
    def subject = new NotificationSynchronizerImpl(clock, dueItemQueryService, notificationPreferences,
            notificationRecipientResolver, notificationRepository, Mock(PlatformTransactionManager))

    def setup() {
        this.notificationRecipientResolver.resolve(_ as DueItem) >> [USER_ID]
    }

    def "Stage due within the notice period should get a notification"() {
        given:
            this.existing([])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(2))]))
            this.preference(true, [10, 3, 1])
        when:
            this.subject.synchronize(STAGE)
        then:
            1 * this.notificationRepository.save({ Notification notification ->
                notification.userId == USER_ID && notification.type == NotificationType.STAGE_DEADLINE
                        && notification.dueDate == TODAY.plusDays(2) && notification.itemName == "Concept"
                        && !notification.read && notification.remindedDaysBefore == 3
            })
    }

    def "Stage due after the notice period should get no notification"() {
        given:
            this.existing([])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(10))]))
            this.preference(true, [3])
        when:
            this.subject.synchronize(STAGE)
        then:
            0 * this.notificationRepository.save(_)
    }

    def "Type turned off should get no notification and lose the ones it had, read ones too"() {
        given:
            def unread = new Notification(USER_ID, stageDue(TODAY.plusDays(2)), 3)
            def read = this.read(new Notification(USER_ID, stageDue(TODAY.plusDays(1)), 3))
            this.existing([unread, read])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(2))]))
            this.preference(false, [3])
        when:
            this.subject.synchronize(STAGE)
        then:
            0 * this.notificationRepository.save(_)
            1 * this.notificationRepository.deleteAll([unread, read])
    }

    def "Synchronizing again should keep the notification and refresh its names, not add another"() {
        given:
            def notification = new Notification(USER_ID, stageDue(TODAY.plusDays(2)), 3)
            this.existing([notification])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(2), "Renamed")]))
            this.preference(true, [3])
        when:
            this.subject.synchronize(STAGE)
        then:
            0 * this.notificationRepository.save(_)
            1 * this.notificationRepository.deleteAll([])
            notification.itemName == "Renamed"
    }

    def "Moved deadline should replace the notifications of the old date, read ones too"() {
        given:
            def unread = new Notification(USER_ID, stageDue(TODAY.plusDays(1)), 3)
            def read = this.read(new Notification(USER_ID, stageDue(TODAY), 3))
            this.existing([unread, read])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(3))]))
            this.preference(true, [3])
        when:
            this.subject.synchronize(STAGE)
        then:
            1 * this.notificationRepository.save({ Notification notification -> notification.dueDate == TODAY.plusDays(3) })
            1 * this.notificationRepository.deleteAll([unread, read])
    }

    def "Finished stage should lose its notifications, read ones too"() {
        given:
            def unread = new Notification(USER_ID, stageDue(TODAY.plusDays(1)), 3)
            def read = this.read(new Notification(OTHER_USER_ID, stageDue(TODAY.plusDays(1)), 3))
            this.existing([unread, read])
            this.dueDates(ItemDueDates.of([]))
        when:
            this.subject.synchronize(STAGE)
        then:
            1 * this.notificationRepository.deleteAll([unread, read])
    }

    def "Reaching a reminder closer to the due date should make the read notification unread again"() {
        given:
            def notification = this.read(new Notification(USER_ID, stageDue(TODAY.plusDays(1)), 10))
            this.existing([notification])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(1))]))
            this.preference(true, [10, 1])
        when:
            this.subject.synchronize(STAGE)
        then:
            0 * this.notificationRepository.save(_)
            1 * this.notificationRepository.deleteAll([])
            !notification.read
            notification.remindedDaysBefore == 1
    }

    def "Read notification should stay read until the next reminder"() {
        given:
            def notification = this.read(new Notification(USER_ID, stageDue(TODAY.plusDays(5)), 10))
            this.existing([notification])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(5))]))
            this.preference(true, [10, 1])
        when:
            this.subject.synchronize(STAGE)
        then:
            notification.read
            notification.remindedDaysBefore == 10
    }

    def "Read notification should stay read when the reminder it came from is removed"() {
        given:
            def notification = this.read(new Notification(USER_ID, stageDue(TODAY.plusDays(2)), 3))
            this.existing([notification])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(2))]))
            this.preference(true, [10])
        when:
            this.subject.synchronize(STAGE)
        then:
            notification.read
            notification.remindedDaysBefore == 3
    }

    def "Gone item should lose all its notifications, read ones too"() {
        given:
            def read = this.read(new Notification(USER_ID, stageDue(TODAY.plusDays(1)), 3))
            this.existing([read])
            this.dueDates(ItemDueDates.itemGone())
        when:
            this.subject.synchronize(STAGE)
        then:
            1 * this.notificationRepository.deleteAll([read])
            0 * this.notificationRepository.save(_)
    }

    def "Deadline passed should keep the unread notification while the stage is open"() {
        given:
            def unread = new Notification(USER_ID, stageDue(TODAY.minusDays(2)), 3)
            this.existing([unread])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.minusDays(2))]))
            this.preference(true, [3])
        when:
            this.subject.synchronize(STAGE)
        then:
            0 * this.notificationRepository.save(_)
            1 * this.notificationRepository.deleteAll([])
    }

    def "Unpaid company cost charges should be read from the earliest one already notified about"() {
        given:
            def companyCost = new ItemRef(NotificationItemType.COMPANY_COST, 9L)
            def charge = new DueItem(NotificationType.COMPANY_COST_PAYMENT, NotificationItemType.COMPANY_COST, 9L,
                    null, null, TODAY.minusDays(20), null, "Rent", null, null, true)
            this.notificationRepository.findByItemTypeAndItemId(NotificationItemType.COMPANY_COST, 9L) >>
                    [new Notification(USER_ID, charge, 3)]
        when:
            this.subject.synchronize(companyCost)
        then:
            1 * this.dueItemQueryService.getDueDates(companyCost, TODAY, _ as LocalDateTime, TODAY.minusDays(20)) >>
                    ItemDueDates.of([])
    }

    def "Settings change of one user should leave the notifications of other users as they are"() {
        given:
            def mine = new Notification(USER_ID, stageDue(TODAY.plusDays(2)), 3)
            def others = new Notification(OTHER_USER_ID, stageDue(TODAY.plusDays(2)), 3)
            this.existing([mine, others])
            this.dueDates(ItemDueDates.of([stageDue(TODAY.plusDays(2))]))
            this.preference(false, [3])
            this.dueItemQueryService.getItemsToCheck([NotificationItemType.STAGE] as Set, TODAY, _ as LocalDateTime) >>
                    ([STAGE] as Set)
        when:
            this.subject.synchronizeUserItemTypes(USER_ID, EnumSet.of(NotificationItemType.STAGE))
        then:
            1 * this.notificationRepository.deleteAll([mine])
            0 * this.notificationRepository.deleteAll(_)
    }

    def "Old read notifications should be removed, except overdue installments still unpaid"() {
        when:
            this.subject.removeOldReadNotifications()
        then:
            1 * this.notificationRepository.deleteReadBefore(TODAY.minusDays(90).atTime(9, 0),
                    NotificationType.INSTALLMENT_OVERDUE)
    }

    private void existing(List<Notification> notifications) {
        this.notificationRepository.findByItemTypeAndItemId(NotificationItemType.STAGE, STAGE_ID) >> notifications
    }

    private void dueDates(ItemDueDates dueDates) {
        this.dueItemQueryService.getDueDates(STAGE, TODAY, _ as LocalDateTime, _ as LocalDate) >> dueDates
    }

    private void preference(boolean enabled, List<Integer> daysBefore) {
        this.notificationPreferences.get([USER_ID], NotificationType.STAGE_DEADLINE) >>
                [(USER_ID): new NotificationPreference(enabled, daysBefore)]
    }

    private static Notification read(Notification notification) {
        notification.markRead(TODAY.atStartOfDay())
        return notification
    }

    private static DueItem stageDue(LocalDate dueDate, String name = "Concept") {
        return new DueItem(NotificationType.STAGE_DEADLINE, NotificationItemType.STAGE, STAGE_ID, 1L, STAGE_ID,
                dueDate, null, name, "Villa", 5L, false)
    }
}
