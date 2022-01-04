package com.arturjarosz.task.project.status.task.listener.impl

import com.arturjarosz.task.project.model.Project
import com.arturjarosz.task.project.model.Stage
import com.arturjarosz.task.project.model.Task
import com.arturjarosz.task.project.status.stage.StageStatus
import com.arturjarosz.task.project.status.stage.impl.StageWorkflowServiceImpl
import com.arturjarosz.task.project.status.task.TaskStatus
import com.arturjarosz.task.project.utils.ProjectBuilder
import com.arturjarosz.task.project.utils.StageBuilder
import com.arturjarosz.task.project.utils.TaskBuilder
import spock.lang.Specification

class TaskRejectFromToDoListenerTest extends Specification {
    private static final long STAGE_ID = 100L

    def stageWorkflowService = Mock(StageWorkflowServiceImpl)
    def taskRejectFromToDoListener = new TaskRejectFromToDoListener(stageWorkflowService)

    def "rejecting only task in TO_DO status on stage in TO_DO status should not change that stage status"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def stage = this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.TO_DO, Arrays.asList(task))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromToDoListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageWorkflowService.changeStageStatusOnProject(project, STAGE_ID, _ as StageStatus)
    }

    def "rejecting one of the task in TO_DO status on stage in TO_DO status should not change that stage status"() {
        given:
            def task1 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.TO_DO,
                            Arrays.asList(task1, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task1.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromToDoListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageWorkflowService.changeStageStatusOnProject(project, STAGE_ID, _ as StageStatus)
    }

    def "rejecting task in TO_DO status, on stage in IN_PROGRESS status, when rest are only in COMPLETE and REJECTED statuses should change stage status to COMPLETED"() {
        given:
            def task1 = this.createTaskOfGivenStatus(TaskStatus.TO_DO)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.COMPLETED)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task1, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task1.changeStatus(TaskStatus.REJECTED)
            this.taskRejectFromToDoListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageWorkflowService.changeStageStatusOnProject(project, STAGE_ID, StageStatus.COMPLETED)
    }

    private Project createProjectWithGivenStage(Stage stage) {
        new ProjectBuilder().withStage(stage).build()
    }

    private Stage createStageWithIdStatusAndGivenTasks(long stageId, StageStatus stageSta, List<Task> tasks) {
        new StageBuilder().withId(stageId).withStatus(stageSta).withTasks(tasks).build()
    }

    private Task createTaskOfGivenStatus(TaskStatus taskStatus) {
        new TaskBuilder().withStatus(taskStatus).build()
    }
}
