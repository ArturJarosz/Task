package com.arturjarosz.task.project.status.stage.listener.impl

import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.status.project.ProjectStatus
import com.arturjarosz.task.project.status.project.impl.ProjectStatusTransitionServiceImpl
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import com.google.common.collect.Sets
import spock.lang.Specification

class StageRejectFromProgressListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageRejectFromProgressListener = new StageRejectFromProgressListener(projectStatusTransitionService)

    def "Rejecting the only stage from IN_PROGRESS on the project should return project to TO_DO"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToToDo(project)
    }

    def "Rejecting stage from IN_PROGRESS on project with only REJECTED stages should return project to TO_DO"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToToDo(project)
    }

    def "Rejecting stage from IN_PROGRESS on project with stages in REJECTED and TO_DO should return project to TO_DO"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.TO_DO)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToToDo(project)
    }

    def "Rejecting stage from IN_PROGRESS on project with stags in IN_PROGRESS should not change project stage"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromProgressListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Rejecting stage from IN_PROGRESS on project with stages in only DONE statuses should change project status to DONE"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.DONE)
            def stage3 = this.createStageWithStatus(StageStatus.DONE)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.finishWork(project)
    }

    def "Rejecting stage from IN_PROGRESS on project with stage in DONE and REJECTED statuses should change project stage to DONE"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.DONE)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.REJECTED)
            this.stageRejectFromProgressListener.onStageStatusChange(project)
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
