package com.arturjarosz.task.project.status.stage.listener.impl

import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.impl.ProjectStatusTransitionServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import spock.lang.Specification

class StageReopenToInProgressListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageReopenToInProgressListener = new StageReopenToInProgressListener(projectStatusTransitionService)

    def "Reopening the only stage to IN_PROGRESS on project in TO_DO should change project status to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO, Set.of(stage))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageReopenToInProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.startProgress(project)
    }

    def "Reopening stage to IN_PROGRESS on project in TO_DO with stages with statuses in TO_DO and REJECTED should change project status to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.TO_DO)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO,
                            Set.of(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageReopenToInProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.startProgress(project)
    }

    def "Reopening stage to IN_PROGRESS on project in IN_PROGRESS with stages with statuses in IN_PROGRESS and REJECTED should not change project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Set.of(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageReopenToInProgressListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Reopening stage to DONE on project in DONE with stages with statuses in DONE and REJECTED should change project status to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.DONE)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.DONE,
                            Set.of(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageReopenToInProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToProgress(project)
    }

    private Stage createStageWithStatus(StageStatus stageStatus) {
        new StageBuilder().withStatus(stageStatus).build()
    }

    private Project createProjectWithGivenStatusAndStages(ProjectStatus status, Set<Stage> stages) {
        new ProjectBuilder().withStatus(status).withStages(stages).build()
    }
}
