package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.StageStatusDto;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StageMapper {

    @Mapping(source = "stageWorkflow", target = "stageWorkflow")
    @Mapping(source = "stageDto.name", target = "name")
    @Mapping(source = "stageDto.type", target = "stageType")
    Stage mapFromDto(StageDto stageDto, StageWorkflow stageWorkflow);

    @Mapping(source = "stageType", target = "type")
    @Mapping(source = "stage", target = "nextStatuses", qualifiedByName = "getNextStatuses")
    StageDto mapToDto(Stage stage);

    @Named("getNextStatuses")
    default List<StageStatusDto> getNextStatuses(Stage stage) {
        return stage.getStatus().getPossibleStatusTransitions().stream()
                .map(status -> StageStatusDto.fromValue(status.getStatusName()))
                .toList();
    }
}
