package com.arturjarosz.task.project.status.project.listener.impl

import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectStatusTransition
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectFinishWorkListenerTest extends Specification {
    static final LocalDate END_DATE = LocalDate.of(2024, 01, 01)

    def projectFinishWorkListener = new ProjectFinishWorkListener()

    def "Listener should be fired on FINISH_WORK transition"() {
        expect:
            this.projectFinishWorkListener.statusTransition == ProjectStatusTransition.FINISH_WORK
    }

    def "Finishing work on project without end date sets end date to today"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.DONE).build()
        when:
            this.projectFinishWorkListener.onProjectStatusChange(project)
        then:
            project.endDate == LocalDate.now()
    }

    def "Finishing work on project with end date does not change that date"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.DONE).withEndDate(END_DATE).build()
        when:
            this.projectFinishWorkListener.onProjectStatusChange(project)
        then:
            project.endDate == END_DATE
    }
}
