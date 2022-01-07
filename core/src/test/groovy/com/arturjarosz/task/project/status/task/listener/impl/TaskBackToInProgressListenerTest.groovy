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

class TaskBackToInProgressListenerTest extends Specification {
    private static final long STAGE_ID = 100L

    def stageWorkflowService = Mock(StageWorkflowServiceImpl)
    def taskBackToInProgressListener = new TaskBackToInProgressListener(stageWorkflowService)

    def "Changing task status from COMPLETED to IN_PROGRESS in stage in IN_PROGRESS status should not change that stage status"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.COMPLETED)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.IN_PROGRESS)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.IN_PROGRESS,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskBackToInProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            0 * this.stageWorkflowService.changeStageStatusOnProject(project, STAGE_ID, _ as StageStatus)
    }

    def "Changing task status from COMPLETED to IN_PROGRESS in stage in COMPLETED status should change that stage status to IN_PROGRESS"() {
        given:
            def task = this.createTaskOfGivenStatus(TaskStatus.COMPLETED)
            def task2 = this.createTaskOfGivenStatus(TaskStatus.COMPLETED)
            def task3 = this.createTaskOfGivenStatus(TaskStatus.REJECTED)
            def stage =
                    this.createStageWithIdStatusAndGivenTasks(STAGE_ID, StageStatus.COMPLETED,
                            Arrays.asList(task, task2, task3))
            def project = this.createProjectWithGivenStage(stage)
        when:
            task.changeStatus(TaskStatus.IN_PROGRESS)
            this.taskBackToInProgressListener.onTaskStatusChange(project, STAGE_ID)
        then:
            1 * this.stageWorkflowService.changeStageStatusOnProject(project, STAGE_ID, StageStatus.IN_PROGRESS)
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
