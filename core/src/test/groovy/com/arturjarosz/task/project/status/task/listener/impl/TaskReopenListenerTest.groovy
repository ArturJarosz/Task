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

class TaskReopenListenerTest extends Specification {
    private static final long STAGE_ID = 100L

    def stageStatusTransitionService = Mock(StageStatusTransitionService)
    def taskReopenListener = new TaskReopenListener(stageStatusTransitionService)

    def "Reopening task in stage in TO_DO status should not change that stage status"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.TO_DO,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskReopenListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService.reopen(project, STAGE_ID)
    }

    def "Reopening task in stage in IN_PROGRESS status should not change that stage status"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskReopenListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService.reopen(project, STAGE_ID)
    }

    def "Reopening task in stage in DONE status should change that stage status to IN_PROGRESS"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.DONE,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskReopenListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.reopen(project, STAGE_ID)
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
