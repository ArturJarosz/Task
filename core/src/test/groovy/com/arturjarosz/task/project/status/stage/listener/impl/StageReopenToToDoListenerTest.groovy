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

class StageReopenToToDoListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageReopenToToDoListener = new StageReopenToToDoListener(projectStatusTransitionService)

    def "Reopening the only stage to TO_DO on project in OFFER status should not affect project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.OFFER, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageReopenToToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Reopening stage to TO_DO on project in OFFER status with stages in TO_DO and REJECTED statuses should not affect project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.TO_DO)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.OFFER,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageReopenToToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Reopening the only stage to TO_DO on project in TO_DO status should not affect project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageReopenToToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Reopening stage to TO_DO on project in TO_DO status with stages in TO_DO and REJECTED statuses should not affect project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.TO_DO)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageReopenToToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Reopening stage to TO_DO on project in IN_PROGRESS status with stages in IN_PROGRESS should not affect project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageReopenToToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Reopening stage to TO_DO on project in COMPLETED status with stages in IN_PROGRESS should change project status to IN_PROGRESS"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.REJECTED)
            def stage2 = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.COMPLETED,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageReopenToToDoListener.onStageStatusChange(project)
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
