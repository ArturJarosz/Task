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

class StageBackToToDoListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageBackToToDoListener = new StageBackToToDoListener(projectStatusTransitionService)

    def "Changing only stage status from IN_PROGRESS to TO_DO on project should change project status to TO_DO"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageBackToToDoListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToToDo(project)
    }

    def "Changing stage status from IN_PROGRESS to TO_DO when there are only stages in REJECTED should change project status to TO_DO"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage, stage2))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageBackToToDoListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToToDo(project)
    }

    def "Changing stage status from IN_PROGRESS to TO_DO when there are only stages in REJECTED and TO_DO should change project status to TO_DO"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.REJECTED)
            def stage3 = this.createStageWithStatus(StageStatus.TO_DO)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageBackToToDoListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.backToToDo(project)
    }

    def "Changing stage status from IN_PROGRESS to TO_DO when there are some stages in IN_PROGRESS should not change project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageBackToToDoListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    def "Changing stage status from IN_PROGRESS to TO_DO when there are stages in COMPLETED should not change project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage2 = this.createStageWithStatus(StageStatus.COMPLETED)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS, Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.TO_DO)
            this.stageBackToToDoListener.onStageStatusChange(project)
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
