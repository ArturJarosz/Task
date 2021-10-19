package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.model.Supply;
import org.mapstruct.factory.Mappers;

public interface SupplyDtoMapper {
    SupplyDtoMapper INSTANCE = Mappers.getMapper(SupplyDtoMapper.class);

    default Supply supplyDtoToSupply(SupplyDto supplyDto) {
        return new Supply(supplyDto.getName(), supplyDto.getSupplierId(), supplyDto.getValue(),
                supplyDto.getHasInvoice(), supplyDto.getPayable());
    }

    SupplyDto supplyToSupplyDto(Supply supply);
}
