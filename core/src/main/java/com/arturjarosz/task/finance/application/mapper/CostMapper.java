package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.finance.model.Cost;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CostMapper {

    Cost mapFromDto(CostDto costDto);

    @Mapping(source = "value", target = "value")
    CostDto mapToDto(Cost cost);

}
