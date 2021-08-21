package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.SupervisionVisitDto;
import com.arturjarosz.task.project.model.SupervisionVisit;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupervisionVisitDtoMapper {
    SupervisionVisitDtoMapper INSTANCE = Mappers.getMapper(SupervisionVisitDtoMapper.class);

    SupervisionVisitDto supervisionVisitDtoFromSupervisionVision(SupervisionVisit supervisionVisit);
}
