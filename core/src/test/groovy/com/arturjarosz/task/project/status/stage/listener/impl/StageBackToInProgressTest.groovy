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

class StageBackToInProgressTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageBackToInProgress = new StageBackToInProgress(projectStatusTransitionService)

    def "Changing status from COMPLETED to IN_PROGRESS of the only stage on project in COMPLETE status pushes project back to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.COMPLETED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.COMPLETED, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageBackToInProgress.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToProgress(project)
    }

    def "Changing status from COMPLETED to IN_PROGRESS of the stage on project in COMPLETE status with other stages pushes project back to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.COMPLETED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.COMPLETED,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageBackToInProgress.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToProgress(project)
    }

    def "Changing status from COMPLETED to IN_PROGRESS of the stage on project in IN_PROGRESS status, with other stages does not changes project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageBackToInProgress.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    private Stage createStageWithStatus(StageStatus stageStatus) {
        new StageBuilder().withStatus(stageStatus).build()
    }

    private Project createProjectWithGivenStatusAndStages(ProjectStatus status, Set<Stage> stages) {
        new ProjectBuilder().withStatus(status).withStages(stages).build()
    }
}
