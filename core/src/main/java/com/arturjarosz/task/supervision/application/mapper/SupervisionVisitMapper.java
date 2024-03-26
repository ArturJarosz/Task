package com.arturjarosz.task.supervision.application.mapper;

import com.arturjarosz.task.dto.SupervisionVisitDto;
import com.arturjarosz.task.supervision.model.SupervisionVisit;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupervisionVisitMapper {
    SupervisionVisitDto mapToDto(SupervisionVisit supervisionVisit);
}
