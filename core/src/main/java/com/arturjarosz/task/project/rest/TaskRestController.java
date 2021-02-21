package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.project.application.TaskApplicationService;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("projects")
public class TaskRestController {

    private TaskApplicationService taskApplicationService;

    TaskRestController(TaskApplicationService taskApplicationService) {

        this.taskApplicationService = taskApplicationService;
    }

    @PostMapping("{projectId}/stages/{stageId}/tasks")
    public ResponseEntity<CreatedEntityDto> createTask(@PathVariable("projectId") Long projectId,
                                                       @PathVariable("stageId") Long stageId,
                                                       @RequestBody TaskDto taskDto) {
        return new ResponseEntity<>(this.taskApplicationService.createTask(projectId, stageId, taskDto),
                HttpStatus.CREATED);
    }

    @DeleteMapping("{projectId}/stages/{stageId}/tasks/{taskId}")
    public ResponseEntity<Void> deleteTask(@PathVariable("projectId") Long projectId,
                                           @PathVariable("stageId") Long stageId,
                                           @PathVariable("taskId") Long taskId) {
        this.taskApplicationService.deleteTask(projectId, stageId, taskId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PutMapping("{projectId}/stages/{stageId}/tasks/{taskId}")
    public ResponseEntity<Void> updateTask(@PathVariable("projectId") Long projectId,
                                           @PathVariable("stageId") Long stageId,
                                           @PathVariable("taskId") Long taskId,
                                           TaskDto taskDto) {
        this.taskApplicationService.updateTask(projectId, stageId, taskId, taskDto);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    //TODO: TA-78 add get Task

    //TODO: TA-78 add get Task list
}
