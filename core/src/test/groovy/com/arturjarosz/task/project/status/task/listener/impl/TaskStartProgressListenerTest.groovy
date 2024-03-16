package com.arturjarosz.task.project.status.task.listener.impl

import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.status.stage.StageStatusTransitionService
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.utils.ProjectBuilder
import com.arturjarosz.task.utils.StageBuilder
import com.arturjarosz.task.utils.TaskBuilder
import spock.lang.Specification

class TaskStartProgressListenerTest extends Specification {
    private static final long STAGE_ID = 100L

    def stageStatusTransitionService = Mock(StageStatusTransitionService)
    def taskStartProgressListener = new TaskStartProgressListener(stageStatusTransitionService)

    def "When the only task on stage changes its status from TO_DO to IN_PROGRESS and stage is in TO_DO, stage changes status to IN_PROGRESS"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def stage = this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.TO_DO, Arrays.asList(task))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskStartProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.startProgress(project, STAGE_ID)
    }

    def "When one of the tasks on stage changes its status from TO_DO to IN_PROGRESS and stage is in TO_DO, stage changes status to IN_PROGRESS"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.TO_DO,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskStartProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.startProgress(project, STAGE_ID)
    }

    def "When one of the tasks on stage changes its status from TO_DO to IN_PROGRESS and stage is IN_PROGRESS, stage stays IN_PROGRESS"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskStartProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService.startProgress(project, STAGE_ID)
    }

    private Project createProjectWithGivenStage(Stage stage) {
        new ProjectBuilder().withStage(stage).build()
    }

    private Stage createStageWithIdStatusAndGivenTasks(long stageId, StageStatus stageStatus, List<Task> tasks) {
        new StageBuilder().withId(stageId).withStatus(stageStatus).withTasks(tasks).build()
    }

    private Task createTaskOfGivenStatus(TaskStatus taskStatus) {
        new TaskBuilder().withStatus(taskStatus).build()
    }
}
