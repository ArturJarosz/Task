package com.arturjarosz.task.notification.application

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository
import com.arturjarosz.task.architect.model.Architect
import com.arturjarosz.task.notification.domain.DueItem
import com.arturjarosz.task.notification.model.NotificationItemType
import com.arturjarosz.task.notification.model.NotificationType
import com.arturjarosz.task.user.infrastructure.repository.UserRepository
import com.arturjarosz.task.user.model.User
import spock.lang.Specification

import java.time.LocalDate

class NotificationRecipientResolverTest extends Specification {

    static final ARCHITECT_ID = 5L
    static final USER_ID = 7L

    def architectRepository = Mock(ArchitectRepository)
    def userRepository = Mock(UserRepository)
    def subject = new NotificationRecipientResolver(architectRepository, userRepository)

    def "Item of an architect linked to a user should go to that user"() {
        given:
            def architect = Mock(Architect) { getUserId() >> USER_ID }
            this.architectRepository.findById(ARCHITECT_ID) >> Optional.of(architect)
        expect:
            this.subject.resolve(dueItem(ARCHITECT_ID, false)) == [USER_ID] as Set
    }

    def "Item of an architect without a linked user should go to no one"() {
        given:
            def architect = Mock(Architect) { getUserId() >> null }
            this.architectRepository.findById(ARCHITECT_ID) >> Optional.of(architect)
        expect:
            this.subject.resolve(dueItem(ARCHITECT_ID, false)).isEmpty()
    }

    def "Item without an architect that isn't for everyone should go to no one"() {
        when:
            def recipients = this.subject.resolve(dueItem(null, false))
        then:
            recipients.isEmpty()
            0 * this.userRepository.findAll()
    }

    def "Item for everyone should go to every user"() {
        given:
            this.userRepository.findAll() >> [Mock(User) { getId() >> 1L }, Mock(User) { getId() >> 2L }]
        expect:
            this.subject.resolve(dueItem(null, true)) == [1L, 2L] as Set
    }

    private static DueItem dueItem(Long architectId, boolean forEveryone) {
        return new DueItem(NotificationType.MEETING, NotificationItemType.CALENDAR_EVENT, 1L, null, null,
                LocalDate.of(2026, 11, 16), null, "Site visit", null, architectId, forEveryone)
    }
}
