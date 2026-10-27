package com.arturjarosz.task.project.status.project.listener.impl

import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectStatusTransition
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectReopenToProgressListenerTest extends Specification {
    static final LocalDate START_DATE = LocalDate.of(2024, 01, 01)
    static final LocalDate END_DATE = LocalDate.of(2024, 02, 01)

    def projectReopenToProgressListener = new ProjectReopenToProgressListener()

    def "Listener should be fired on REOPEN_TO_PROGRESS transition"() {
        expect:
            this.projectReopenToProgressListener.statusTransition == ProjectStatusTransition.REOPEN_TO_PROGRESS
    }

    def "Reopening project to progress removes end date and keeps start date"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.IN_PROGRESS)
                    .withStartDate(START_DATE)
                    .withEndDate(END_DATE)
                    .build()
        when:
            this.projectReopenToProgressListener.onProjectStatusChange(project)
        then:
            project.startDate == START_DATE
            project.endDate == null
    }
}
