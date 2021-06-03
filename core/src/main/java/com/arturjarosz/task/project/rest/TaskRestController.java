package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.TaskApplicationService;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.sharedkernel.utils.HttpHeadersBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("projects/{projectId}")
public class TaskRestController {

    private final TaskApplicationService taskApplicationService;

    TaskRestController(TaskApplicationService taskApplicationService) {

        this.taskApplicationService = taskApplicationService;
    }

    @PostMapping("stages/{stageId}/tasks")
    public ResponseEntity<TaskDto> createTask(@PathVariable("projectId") Long projectId,
                                              @PathVariable("stageId") Long stageId,
                                              @RequestBody TaskDto taskDto) {
        TaskDto createdTaskDto = this.taskApplicationService.createTask(projectId, stageId, taskDto);
        HttpHeaders headers = new HttpHeadersBuilder()
                .withLocation("/stage/{stageId}/tasks/{tasksId}", stageId, createdTaskDto.getId())
                .build();
        return new ResponseEntity<>(createdTaskDto, headers, HttpStatus.CREATED);
    }

    @DeleteMapping("stages/{stageId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable("projectId") Long projectId,
                                           @PathVariable("stageId") Long stageId,
                                           @PathVariable("taskId") Long taskId) {
        this.taskApplicationService.deleteTask(projectId, stageId, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("stages/{stageId}/tasks/{taskId}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable("projectId") Long projectId,
                                              @PathVariable("stageId") Long stageId,
                                              @PathVariable("taskId") Long taskId,
                                              @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(this.taskApplicationService.updateTask(projectId, stageId, taskId, taskDto),
                HttpStatus.OK);
    }

    @PostMapping("stages/{stageId}/tasks/{taskId}/updateStatus")
    public ResponseEntity<TaskDto> updateStatus(@PathVariable("projectId") Long projectId,
                                                @PathVariable("stageId") Long stageId,
                                                @PathVariable("taskId") Long taskId,
                                                @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(this.taskApplicationService.updateTaskStatus(projectId, stageId, taskId, taskDto),
                HttpStatus.OK);
    }

    @GetMapping("stages/{stageId}/tasks/{taskId}")
    public ResponseEntity<TaskDto> getTask(@PathVariable("projectId") Long projectId,
                                           @PathVariable("stageId") Long stageId,
                                           @PathVariable("taskId") Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.getTask(projectId, stageId, taskId), HttpStatus.OK);
    }

    @GetMapping("stages/{stageId}/tasks")
    public ResponseEntity<List<TaskDto>> getTasks(@PathVariable("projectId") Long projectId,
                                                  @PathVariable("stageId") Long stageId) {
        return new ResponseEntity<>(this.taskApplicationService.getTaskList(projectId, stageId), HttpStatus.OK);
    }

    @PostMapping("stages/{stageId}/tasks/{taskId}/reject")
    public ResponseEntity<TaskDto> rejectTask(@PathVariable("projectId") Long projectId,
                                              @PathVariable("stageId") Long stageId,
                                              @PathVariable("taskId") Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.rejectTask(projectId, stageId, taskId), HttpStatus.OK);
    }

    @PostMapping("stages/{stageId}/tasks/{taskId}/reopen")
    public ResponseEntity<TaskDto> reopenTask(@PathVariable("projectId") Long projectId,
                                              @PathVariable("stageId") Long stageId,
                                              @PathVariable("taskId") Long taskId) {
        return new ResponseEntity<>(this.taskApplicationService.reopenTask(projectId, stageId, taskId), HttpStatus.OK);
    }
}
