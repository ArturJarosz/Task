package com.arturjarosz.task.project.status.stage.listener.impl

import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.impl.ProjectStatusTransitionServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.google.common.collect.Sets
import spock.lang.Specification

class StageCreateListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageCreateListener = new StageCreateListener(projectStatusTransitionService)

    def "Creating new stage on project in TO_DO status should not change project status"(){
        given:
            def stage = this.createStageWithStatus(null)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageCreateListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Creating new stage on project in IN_PROGRESS status should not change project status"(){
        given:
            def stage = this.createStageWithStatus(null)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageCreateListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Creating new stage on project in DONE status should reopen project"(){
        given:
            def stage = this.createStageWithStatus(null)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.DONE, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageCreateListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToProgress(project)
    }

    private Project createProjectWithGivenStatusAndStages(ProjectStatus status, Set<Stage> stages) {
        new ProjectBuilder().withStatus(status).withStages(stages).build()
    }

    private Stage createStageWithStatus(StageStatus stageStatus) {
        new StageBuilder().withStatus(stageStatus).build()
    }
}
