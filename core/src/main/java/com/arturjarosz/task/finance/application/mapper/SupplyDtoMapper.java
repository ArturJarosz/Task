package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.finance.application.dto.SupplyDto;
import com.arturjarosz.task.finance.model.Supply;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SupplyDtoMapper {
    SupplyDtoMapper INSTANCE = Mappers.getMapper(SupplyDtoMapper.class);

    default Supply supplyDtoToSupply(SupplyDto supplyDto) {
        return new Supply(supplyDto.getName(), supplyDto.getSupplierId(), supplyDto.getValue(),
                supplyDto.getHasInvoice(), supplyDto.getPayable());
    }

    @Mapping(source = "projectId", target = "projectId")
    SupplyDto supplyToSupplyDto(Supply supply, Long projectId);
}
