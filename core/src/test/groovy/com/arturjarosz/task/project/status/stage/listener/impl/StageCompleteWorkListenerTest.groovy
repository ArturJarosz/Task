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

class StageCompleteWorkListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageCompleteWorkListener = new StageCompleteWorkListener(projectStatusTransitionService)

    def "Complete work on only stage in the project, should complete work on the project as well"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.COMPLETED)
            this.stageCompleteWorkListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.completeWork(project)
    }

    def "Complete work on stage, where rest of the stages are in the REJECTED or COMPLETE, should complete work on the project"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.COMPLETED)
            this.stageCompleteWorkListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.completeWork(project)
    }

    def "Complete work on stage, where there is at least one stage in IN_PROGRESS, status od the project should not change"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage3 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.COMPLETED)
            this.stageCompleteWorkListener.onStageStatusChange(project)
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
