package com.arturjarosz.task.project.status.project.listener.impl

import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectStatusTransition
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectStartProgressListenerTest extends Specification {
    static final LocalDate START_DATE = LocalDate.of(2024, 01, 01)

    def projectStartProgressListener = new ProjectStartProgressListener()

    def "Listener should be fired on START_PROGRESS transition"() {
        expect:
            this.projectStartProgressListener.statusTransition == ProjectStatusTransition.START_PROGRESS
    }

    def "Starting progress on project without start date sets start date to today"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.IN_PROGRESS).build()
        when:
            this.projectStartProgressListener.onProjectStatusChange(project)
        then:
            project.startDate == LocalDate.now()
    }

    def "Starting progress on project with start date does not change that date"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.IN_PROGRESS).withStartDate(START_DATE).build()
        when:
            this.projectStartProgressListener.onProjectStatusChange(project)
        then:
            project.startDate == START_DATE
    }
}
