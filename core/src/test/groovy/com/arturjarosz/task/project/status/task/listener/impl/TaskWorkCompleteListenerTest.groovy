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

class TaskWorkCompleteListenerTest extends Specification {
    private static final long STAGE_ID = 100L

    def stageStatusTransitionService = Mock(StageStatusTransitionService)
    def taskWorkCompleteListener = new TaskWorkCompleteListener(stageStatusTransitionService)

    def "When finishing work on the only task on stage in IN_PROGRESS status should change status of that stage to DONE"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS, Arrays.asList(task))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.DONE)
            this.taskWorkCompleteListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.completeWork(project, STAGE_ID)
    }

    def "When finishing work on the task on stage in IN_PROGRESS, and rest tasks are REJECTED, status should change status of that stage to DONE"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS, Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.DONE)
            this.taskWorkCompleteListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.completeWork(project, STAGE_ID)
    }

    def "When finishing work on the task on stage in IN_PROGRESS, and rest tasks are REJECTED or DONE, status should change status of that stage to DONE"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS, Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.DONE)
            this.taskWorkCompleteListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageStatusTransitionService.completeWork(project, STAGE_ID)
    }

    def "When finishing work on the task on stage in IN_PROGRESS, and there is at least on task in TO_DO, status of stage should not change"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS, Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.DONE)
            this.taskWorkCompleteListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService.completeWork(project, STAGE_ID)
    }

    def "When finishing work on the task on stage in IN_PROGRESS, and there is at least on task in IN_PROGRESS, status of stage should not change"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.DONE)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS, Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.DONE)
            this.taskWorkCompleteListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageStatusTransitionService.completeWork(project, STAGE_ID)
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
