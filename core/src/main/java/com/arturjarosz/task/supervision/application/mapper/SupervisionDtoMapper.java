package com.arturjarosz.task.supervision.application.mapper;

import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.model.Supervision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupervisionDtoMapper {
    SupervisionDtoMapper INSTANCE = Mappers.getMapper(SupervisionDtoMapper.class);

    @Mapping(target = "hasInvoice", source = "financialData.hasInvoice")
    SupervisionDto supervisionToSupervisionDto(Supervision supervision);
}
