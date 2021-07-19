package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.project.model.Supervision;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupervisionDtoMapper {
    SupervisionDtoMapper INSTANCE = Mappers.getMapper(SupervisionDtoMapper.class);

    SupervisionDto supervisionToSupervisionDto(Supervision supervision);
}
