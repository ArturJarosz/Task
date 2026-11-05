package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.dto.TimeEntryDto;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.TaskValidator;
import com.arturjarosz.task.project.application.TimeEntryApplicationService;
import com.arturjarosz.task.project.application.TimeEntryValidator;
import com.arturjarosz.task.project.application.mapper.TimeEntryMapper;
import com.arturjarosz.task.project.infrastructure.repositor.TimeEntryRepository;
import com.arturjarosz.task.project.model.TimeEntry;
import com.arturjarosz.task.project.query.TimeEntryQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class TimeEntryApplicationServiceImpl implements TimeEntryApplicationService {

    private final ProjectValidator projectValidator;
    private final StageValidator stageValidator;
    private final TaskValidator taskValidator;
    private final TimeEntryValidator timeEntryValidator;
    private final TimeEntryQueryService timeEntryQueryService;
    private final TimeEntryRepository timeEntryRepository;
    private final TimeEntryMapper timeEntryMapper;

    // the existence checks load the project lazily, which needs a transaction
    @Transactional
    @Override
    public List<TimeEntryDto> getTimeEntries(Long projectId, Long stageId, Long taskId) {
        LOG.info("Loading time entries of Task with id {}", taskId);
        this.validateTaskExistence(projectId, stageId, taskId);
        return this.timeEntryQueryService.getTimeEntriesForTask(taskId)
                .stream()
                .map(this.timeEntryMapper::mapToDto)
                .toList();
    }

    @Transactional
    @Override
    public TimeEntryDto createTimeEntry(Long projectId, Long stageId, Long taskId, TimeEntryDto timeEntryDto) {
        LOG.info("Logging time on Task with id {}", taskId);
        this.validateTaskExistence(projectId, stageId, taskId);
        this.timeEntryValidator.validateTimeEntryDto(timeEntryDto);
        var timeEntry = new TimeEntry(taskId, timeEntryDto.getArchitectId(), timeEntryDto.getWorkDate(),
                timeEntryDto.getDurationMinutes(), StringUtils.trimToNull(timeEntryDto.getNote()));
        timeEntry = this.timeEntryRepository.save(timeEntry);
        LOG.info("Time entry with id {} created.", timeEntry.getId());
        return this.timeEntryMapper.mapToDto(timeEntry);
    }

    @Transactional
    @Override
    public TimeEntryDto updateTimeEntry(Long projectId, Long stageId, Long taskId, Long timeEntryId,
            TimeEntryDto timeEntryDto) {
        LOG.info("Updating time entry with id {} of Task with id {}", timeEntryId, taskId);
        this.validateTaskExistence(projectId, stageId, taskId);
        this.timeEntryValidator.validateExistenceOfTimeEntryInTask(taskId, timeEntryId);
        this.timeEntryValidator.validateTimeEntryDto(timeEntryDto);
        var timeEntry = this.timeEntryRepository.findById(timeEntryId).orElseThrow(ResourceNotFoundException::new);
        timeEntry.update(timeEntryDto.getArchitectId(), timeEntryDto.getWorkDate(),
                timeEntryDto.getDurationMinutes(), StringUtils.trimToNull(timeEntryDto.getNote()));
        timeEntry = this.timeEntryRepository.save(timeEntry);
        LOG.info("Time entry updated.");
        return this.timeEntryMapper.mapToDto(timeEntry);
    }

    @Transactional
    @Override
    public void deleteTimeEntry(Long projectId, Long stageId, Long taskId, Long timeEntryId) {
        LOG.info("Deleting time entry with id {} of Task with id {}", timeEntryId, taskId);
        this.validateTaskExistence(projectId, stageId, taskId);
        this.timeEntryValidator.validateExistenceOfTimeEntryInTask(taskId, timeEntryId);
        this.timeEntryRepository.deleteById(timeEntryId);
        LOG.info("Time entry deleted.");
    }

    private void validateTaskExistence(Long projectId, Long stageId, Long taskId) {
        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.taskValidator.validateExistenceOfTaskInStage(stageId, taskId);
    }
}
