package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.StageBasicDto;
import com.arturjarosz.task.project.application.dto.StageDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StageDtoMapper {
    StageDtoMapper INSTANCE = Mappers.getMapper(StageDtoMapper.class);

    Stage stageCreateDtoToStage(StageDto stageDto);

    StageDto stageDtoFromStage(Stage stage);

    StageBasicDto stageToStageBasicDto(Stage stage);
}
