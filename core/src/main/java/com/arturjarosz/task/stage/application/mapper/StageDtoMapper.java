package com.arturjarosz.task.stage.application.mapper;

import com.arturjarosz.task.stage.application.dto.StageDto;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.stage.status.StageWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface StageDtoMapper {
    StageDtoMapper INSTANCE = Mappers.getMapper(StageDtoMapper.class);

    @Mapping(source = "stageWorkflow", target = "stageWorkflow")
    @Mapping(source = "stageDto.name", target = "name")
    Stage stageCreateDtoToStage(StageDto stageDto, StageWorkflow stageWorkflow);

    StageDto stageDtoFromStage(Stage stage);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "installmentDto", ignore = true)
    @Mapping(target = "note", ignore = true)
    StageDto stageToStageBasicDto(Stage stage);
}
