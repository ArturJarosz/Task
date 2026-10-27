package com.arturjarosz.task.project.status.project.listener.impl

import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.ProjectStatusTransition
import com.arturjarosz.task.utils.ProjectBuilder
import spock.lang.Specification

import java.time.LocalDate

class ProjectResumeWorkListenerTest extends Specification {
    static final LocalDate START_DATE = LocalDate.of(2024, 01, 01)

    def projectResumeWorkListener = new ProjectResumeWorkListener()

    def "Listener should be fired on RESUME_WORK transition"() {
        expect:
            this.projectResumeWorkListener.statusTransition == ProjectStatusTransition.RESUME_WORK
    }

    def "Resuming project without start date sets start date to today"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.IN_PROGRESS).build()
        when:
            this.projectResumeWorkListener.onProjectStatusChange(project)
        then:
            project.startDate == LocalDate.now()
    }

    def "Resuming project with start date does not change that date"() {
        given:
            def project = new ProjectBuilder().withStatus(ProjectStatus.IN_PROGRESS).withStartDate(START_DATE).build()
        when:
            this.projectResumeWorkListener.onProjectStatusChange(project)
        then:
            project.startDate == START_DATE
    }
}
