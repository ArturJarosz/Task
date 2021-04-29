package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CostDtoMapper {
    CostDtoMapper INSTANCE = Mappers.getMapper(CostDtoMapper.class);

    @Mapping(source = "value", target = "value", qualifiedByName = "doubleToMoney")
    Cost costCreateDtoToCost(CostDto costDto);

    @Named("doubleToMoney")
    default Money doubleToMoney(Double value) {
        return new Money(value);
    }

    @Mapping(source = "value", target = "value", qualifiedByName = "moneyToDouble")
    CostDto costToCostDto(Cost cost);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money money) {
        return money.getValue().doubleValue();
    }
}
