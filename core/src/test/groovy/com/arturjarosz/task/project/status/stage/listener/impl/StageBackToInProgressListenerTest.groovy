package com.arturjarosz.task.project.status.stage.listener.impl

import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.impl.ProjectStatusTransitionServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import spock.lang.Specification

class StageBackToInProgressListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageBackToInProgress = new StageBackToInProgressListener(projectStatusTransitionService)

    def "Changing status from DONE to IN_PROGRESS of the only stage on project in DONE status pushes project back to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.DONE)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.DONE, Set.of(stage))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageBackToInProgress.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToProgress(project)
    }

    def "Changing status from DONE to IN_PROGRESS of the stage on project in DONE status with other stages pushes project back to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.DONE)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.DONE)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.DONE,
                            Set.of(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageBackToInProgress.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToProgress(project)
    }

    def "Changing status from DONE to IN_PROGRESS of the stage on project in IN_PROGRESS status, with other stages does not changes project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.DONE)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Set.of(stage, stage2, stage3))
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
