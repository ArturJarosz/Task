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

class StageRejectFromToDoListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageRejectFromToDoListener = new StageRejectFromToDoListener(projectStatusTransitionService)

    def "Rejecting the only stage from status TO_DO on the project in TO_DO status should not change project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.TO_DO)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Rejecting stage from status TO_DO on the project in IN_PROGRESS with other stages in IN_PROGRESS statuses should not affect project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.TO_DO)
            def stage2 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Rejecting stage from status TO_DO on the project in IN_PROGRESS with other stages in COMPLETED and REJECTED statuses should make project COMPLETED"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.TO_DO)
            def stage2 = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromToDoListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.finishWork(project)
    }

    private Stage createStageWithStatus(StageStatus stageStatus) {
        new StageBuilder().withStatus(stageStatus).build()
    }

    private Project createProjectWithGivenStatusAndStages(ProjectStatus status, Set<Stage> stages) {
        new ProjectBuilder().withStatus(status).withStages(stages).build()
    }
}
