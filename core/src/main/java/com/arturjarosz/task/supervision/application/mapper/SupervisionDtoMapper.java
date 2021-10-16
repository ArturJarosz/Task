package com.arturjarosz.task.supervision.application.mapper;

import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.model.Supervision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.math.BigDecimal;

@Mapper
public interface SupervisionDtoMapper {
    SupervisionDtoMapper INSTANCE = Mappers.getMapper(SupervisionDtoMapper.class);

    @Mapping(target = "hasInvoice", source = "financialData.hasInvoice")
    @Mapping(target = "value", source = "financialData.value", qualifiedByName = "moneyToBigDecimal")
    SupervisionDto supervisionToSupervisionDto(Supervision supervision);

    @Named("moneyToBigDecimal")
    default BigDecimal moneyToBigDecimal(Money money){
        return money.getValue();
    }

}
