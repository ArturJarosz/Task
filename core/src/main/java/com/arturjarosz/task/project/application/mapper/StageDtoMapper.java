package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StageDtoMapper {
    StageDtoMapper INSTANCE = Mappers.getMapper(StageDtoMapper.class);

    @Mapping(source = "stageWorkflow", target = "stageWorkflow")
    @Mapping(source = "stageDto.name", target = "name")
    @Mapping(source = "stageDto.type", target = "stageType")
    Stage stageCreateDtoToStage(StageDto stageDto, StageWorkflow stageWorkflow);

    @Mapping(source = "stageType", target = "type")
    StageDto stageDtoFromStage(Stage stage);

    @Mapping(target = "startDate", ignore = true)
    @Mapping(target = "endDate", ignore = true)
    @Mapping(target = "installment", ignore = true)
    @Mapping(target = "note", ignore = true)
    StageDto stageToStageBasicDto(Stage stage);
}
