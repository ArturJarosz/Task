package com.arturjarosz.task.notification.model

import spock.lang.Specification

import java.time.LocalDate

class NotificationTypeTest extends Specification {

    static final TODAY = LocalDate.of(2026, 11, 15)

    def "#type due #dueDate with reminders #daysBefore should be notified by reminder #reminder today"() {
        expect:
            def found = type.reminderOn(TODAY, dueDate, daysBefore)
            (found.present ? found.asInt : null) == reminder
        where:
            type                                 | dueDate             | daysBefore || reminder
            NotificationType.STAGE_DEADLINE      | TODAY.plusDays(3)   | [3]        || 3
            NotificationType.STAGE_DEADLINE      | TODAY               | [3]        || 3
            NotificationType.STAGE_DEADLINE      | TODAY.plusDays(4)   | [3]        || null
            NotificationType.STAGE_DEADLINE      | TODAY.minusDays(1)  | [3]        || null
            NotificationType.CONTRACT_DEADLINE   | TODAY.plusDays(15)  | [7]        || null
            NotificationType.CONTRACT_DEADLINE   | TODAY.plusDays(10)  | [10, 1]    || 10
            NotificationType.CONTRACT_DEADLINE   | TODAY.plusDays(5)   | [10, 1]    || 10
            NotificationType.CONTRACT_DEADLINE   | TODAY.plusDays(1)   | [10, 1]    || 1
            NotificationType.CONTRACT_DEADLINE   | TODAY               | [10, 1]    || 1
            NotificationType.CONTRACT_DEADLINE   | TODAY.plusDays(1)   | []         || null
            NotificationType.INSTALLMENT_PAYMENT | TODAY.plusDays(3)   | [3]        || 3
            NotificationType.INSTALLMENT_OVERDUE | TODAY.minusDays(10) | []         || 0
            NotificationType.MEETING             | TODAY.plusDays(1)   | [1]        || 1
            NotificationType.MEETING             | TODAY.plusDays(2)   | [1]        || null
            NotificationType.MEETING             | TODAY.minusDays(1)  | [1]        || 1
            NotificationType.STAGE_DEADLINE      | TODAY               | [0]        || 0
    }

    def "Overdue installment should follow the installment payment setting"() {
        expect:
            NotificationType.INSTALLMENT_OVERDUE.settingType == NotificationType.INSTALLMENT_PAYMENT
            !NotificationType.INSTALLMENT_OVERDUE.setByUser
            NotificationType.values().findAll { it != NotificationType.INSTALLMENT_OVERDUE }.every { it.setByUser }
    }
}
