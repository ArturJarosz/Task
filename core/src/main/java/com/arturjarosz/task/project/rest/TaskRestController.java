package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.application.TaskApplicationService;
import com.arturjarosz.task.rest.TaskApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TaskRestController implements TaskApi {

    @NonNull
    private final TaskApplicationService taskApplicationService;

    @Override
    public ResponseEntity<TaskDto> createTask(TaskDto taskDto, Long projectId, Long stageId) {
        var createdTaskDto = this.taskApplicationService.createTask(projectId, stageId, taskDto);
        var headers = new HttpHeadersBuilder()
                .withLocation("/stage/{stageId}/tasks/{tasksId}", stageId, createdTaskDto.getId())
                .build();
        return new ResponseEntity<>(createdTaskDto, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<Void> deleteTask(Long projectId, Long stageId, Long taskId) {
        this.taskApplicationService.deleteTask(projectId, stageId, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskDto> updateTask(TaskDto taskDto, Long projectId, Long stageId, Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.updateTask(projectId, stageId, taskId, taskDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskDto> updateStatus(TaskDto taskDto, Long projectId, Long stageId, Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.updateTaskStatus(projectId, stageId, taskId, taskDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskDto> getTask(Long projectId, Long stageId, Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.getTask(projectId, stageId, taskId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TaskDto>> getTasksForStage(Long projectId, Long stageId) {
        return new ResponseEntity<>(this.taskApplicationService.getTaskList(projectId, stageId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskDto> rejectTask(Long projectId, Long stageId, Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.rejectTask(projectId, stageId, taskId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskDto> reopenTask(Long projectId, Long stageId, Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.reopenTask(projectId, stageId, taskId), HttpStatus.OK);
    }
}
