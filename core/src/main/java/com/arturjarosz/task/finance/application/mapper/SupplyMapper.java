package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.finance.model.Supply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface SupplyMapper {

    default Supply mapFromDto(SupplyDto supplyDto) {
        return new Supply(supplyDto.getName(), supplyDto.getSupplierId(), supplyDto.getValue(),
                supplyDto.getHasInvoice(), supplyDto.getPayable());
    }

    @Mapping(source = "projectId", target = "projectId")
    SupplyDto mapToDto(Supply supply, Long projectId);
}
