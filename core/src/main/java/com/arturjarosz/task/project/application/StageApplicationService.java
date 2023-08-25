package com.arturjarosz.task.project.application;

import com.arturjarosz.task.dto.StageDto;

import java.util.List;

public interface StageApplicationService {

    /**
     * Creates new Stage from StageDto and adds it to Project with provided id.
     * If project does not exit, then exception is thrown.
     */
    StageDto createStage(Long projectId, StageDto stageDto);

    /**
     * Removes stage with stageId, from project of given projectId.
     * If project or stage does not exist,
     * then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    void removeStage(Long projectId, Long stageId);

    /**
     * Updates stage with given stageId on project with projectId according to data provided with StageDto.
     * If project or stage does not exist, then new
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     */
    StageDto updateStage(Long projectId, Long stageId, StageDto stageDto);

    /**
     * Return Stage of given stageId for Project with given projectId. If Project or Stage do not exist, then
     * new exception will be thrown.
     */
    StageDto getStage(Long projectId, Long stageId);

    /**
     * Return List of StageBasicDto for Project with given projectId.
     */
    List<StageDto> getStageListForProject(Long projectId);

    /**
     * Set Stage with stageId as rejected for Project with projectId. If Project or Stage does not exist, new exception
     * will be thrown.
     */
    StageDto rejectStage(Long projectId, Long stageId);

    /**
     * Reopen rejected Stage with stageId on Project with projectId. If Stage or Project does not exist, then new
     * exception will be thrown.
     */
    StageDto reopenStage(Long projectId, Long stageId);
}
