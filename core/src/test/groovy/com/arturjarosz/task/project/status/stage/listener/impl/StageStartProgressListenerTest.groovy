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

class StageStartProgressListenerTest extends Specification {

    def projectStatusTransitionService = Mock(ProjectStatusTransitionServiceImpl)
    def stageStartProgressListener = new StageStartProgressListener(projectStatusTransitionService)

    def "Starting work on the only stage on the project starts work on the project as well"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.TO_DO)
            def project = this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO, Sets.newHashSet(stage))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageStartProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.startProgress(project)
    }

    def "Starting work on first stage on the project starts work on that project as well"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.TO_DO)
            def stage2 = this.createStageWithStatus(StageStatus.TO_DO)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.TO_DO,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageStartProgressListener.onStageStatusChange(project)
        then:
            1 * projectStatusTransitionService.startProgress(project)
    }

    def "Starting work on stage on project that was already in IN PROGRESS does not change project status"() {
        given:
            def stage = this.createStageWithStatus(StageStatus.TO_DO)
            def stage2 = this.createStageWithStatus(StageStatus.IN_PROGRESS)
            def stage3 = this.createStageWithStatus(StageStatus.REJECTED)
            def project =
                    this.createProjectWithGivenStatusAndStages(ProjectStatus.IN_PROGRESS,
                            Sets.newHashSet(stage, stage2, stage3))
        when:
            stage.changeStatus(StageStatus.IN_PROGRESS)
            this.stageStartProgressListener.onStageStatusChange(project)
        then:
            0 * projectStatusTransitionService._
    }

    private Project createProjectWithGivenStatusAndStages(ProjectStatus status, Set<Stage> stages) {
        new ProjectBuilder().withStatus(status).withStages(stages).build()
    }

    private Stage createStageWithStatus(StageStatus stageStatus) {
        new StageBuilder().withStatus(stageStatus).build()
    }
}
