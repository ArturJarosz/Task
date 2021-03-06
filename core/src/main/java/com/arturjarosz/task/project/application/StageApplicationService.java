package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;

import java.util.List;

public interface StageApplicationService {

    /**
     * Creates new Stage from StageDto and adds it to Project with provided id.
     * If project does not exit, then exception is thrown.
     *
     * @param stageDto
     * @return
     */
    CreatedEntityDto createStage(Long projectId, StageDto stageDto);

    /**
     * Removes stage with stageId, from project of given projectId.
     * If project or stage does not exist,
     * then {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     *
     * @param projectId
     * @param stageId
     */
    void removeStage(Long projectId, Long stageId);

    /**
     * Updates stage with given stageId on project with projectId according to data provided with StageDto.
     * If project or stage doesn not exist, then new
     * {@link com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException} is thrown.
     *
     * @param projectId
     * @param stageId
     * @param stageDto
     */
    void updateStage(Long projectId, Long stageId, StageDto stageDto);

    /**
     * Return Stage of given stageId for Project with given projectId. If Project or Stage do not exist, then
     * new exception will be thrown.
     *
     * @param projectId
     * @param stageId
     * @return
     */
    StageDto getStage(Long projectId, Long stageId);

    /**
     * Return List of StageBasicDto for Project with given projectId.
     *
     * @param projectId
     * @return
     */
    List<StageDto> getStageBasicList(Long projectId);
}
