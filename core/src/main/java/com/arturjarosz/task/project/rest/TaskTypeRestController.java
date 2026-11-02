package com.arturjarosz.task.project.rest;

import com.arturjarosz.task.dto.TaskTypeDto;
import com.arturjarosz.task.project.application.TaskTypeApplicationService;
import com.arturjarosz.task.rest.TaskTypeApi;
import com.arturjarosz.task.sharedkernel.testhelpers.HttpHeadersBuilder;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequiredArgsConstructor
@RestController
public class TaskTypeRestController implements TaskTypeApi {

    @NonNull
    private final TaskTypeApplicationService taskTypeApplicationService;

    @Override
    public ResponseEntity<TaskTypeDto> createTaskType(TaskTypeDto taskTypeDto) {
        var taskType = this.taskTypeApplicationService.create(taskTypeDto);
        var headers = new HttpHeadersBuilder().withLocation("/task-types/{taskTypeId}", taskType.getId())
                .build();
        return new ResponseEntity<>(taskType, headers, HttpStatus.CREATED);
    }

    @Override
    public ResponseEntity<TaskTypeDto> updateTaskType(TaskTypeDto taskTypeDto, Long taskTypeId) {
        return new ResponseEntity<>(this.taskTypeApplicationService.update(taskTypeId, taskTypeDto),
                HttpStatus.OK);
    }

    @Override
    public ResponseEntity<Void> deleteTaskType(Long taskTypeId) {
        this.taskTypeApplicationService.delete(taskTypeId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Override
    public ResponseEntity<TaskTypeDto> getTaskType(Long taskTypeId) {
        return new ResponseEntity<>(this.taskTypeApplicationService.get(taskTypeId), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<List<TaskTypeDto>> getTaskTypes() {
        return new ResponseEntity<>(this.taskTypeApplicationService.getAll(), HttpStatus.OK);
    }
}
