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

class TaskRejectFromProgressListenerTest extends Specification {
    private static final long STAGE_ID = 100L

    def stageStatusTransitionService = Mock(StageStatusTransitionService)
    def taskRejectFromProgressListener = new TaskRejectFromProgressListener(stageStatusTransitionService)

    def "Rejecting the only task from stage in IN_PROGRESS status should change stage status to TO_DO"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS, Arrays.asList(task))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.rejectFromInProgress(project, STAGE_ID)
    }

    def "Rejecting task from stage in IN_PROGRESS status, while other tasks are in TO_DO, should change status of stage to TO_DO"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.rejectFromInProgress(project, STAGE_ID)
    }

    def "Rejecting task from stage in IN_PROGRESS status, while other tasks are in DONE, should change status of stage to DONE"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.completeWork(project, STAGE_ID)
    }

    def "Rejecting task from stage in IN_PROGRESS status, while there is at least on task in IN_PROGRESS status, should not change status of stage"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService._(project, STAGE_ID)
    }

    def "Rejecting task from stage in IN_PROGRESS status, while there are at least one of task in TO_DO and DONE statuses, should not change status of stage"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService._(project, STAGE_ID, _ as StageStatus)
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
