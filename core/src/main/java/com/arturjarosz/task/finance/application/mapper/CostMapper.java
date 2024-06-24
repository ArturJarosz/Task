package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.finance.model.Cost;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, builder = @Builder(disableBuilder = true))
public interface CostMapper {

    Cost mapFromDto(CostDto costDto);

    @Mapping(source = "value", target = "value")
    @Mapping(source = "financialData.hasInvoice", target = "hasInvoice")
    CostDto mapToDto(Cost cost);

}
