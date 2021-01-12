package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.CostDto;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;

@Mapper
public interface CostDtoMapper {
    CostDtoMapper INSTANCE = Mappers.getMapper(CostDtoMapper.class);

    @Mapping(source = "value", target = "value", qualifiedByName = "doubleToMoney")
    Cost costCreateDtoToCost(CostDto costDto);

    @Named("doubleToMoney")
    default Money doubleToMoney(Double value) {
        return new Money(value);
    }

    default CostDto costToCostDto(Cost cost) {
        if (cost == null) {
            return null;
        }

        String name = cost.getName();
        CostCategory category = cost.getCategory();
        Double value = cost.getValue().getValue().doubleValue();
        LocalDate date = cost.getDate();
        String description = cost.getDescription();

        CostDto costDto = new CostDto(name, category, value, date, description);

        costDto.setId(cost.getId());

        return costDto;
    }

}
