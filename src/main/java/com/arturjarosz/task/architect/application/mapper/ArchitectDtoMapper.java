package com.arturjarosz.task.architect.application.mapper;

import com.arturjarosz.task.architect.application.dto.ArchitectBasicDto;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ArchitectDtoMapper {

    ArchitectDtoMapper INSTANCE = Mappers.getMapper(ArchitectDtoMapper.class);

    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    @Mapping(target = "projectsValue", source = "projectsValue", qualifiedByName = "moneyToValue")
    ArchitectDto architectToArchitectDto(Architect architect);

    @Mapping(source = "firstName", target = "firstName")
    @Mapping(source = "lastName", target = "lastName")
    Architect architectBasicDtoToArchitect(ArchitectBasicDto architectBasicDto);

    @Mapping(source = "personName.firstName", target = "firstName")
    @Mapping(source = "personName.lastName", target = "lastName")
    ArchitectBasicDto architectToArchitectBasicDto(Architect architect);

    @Named("moneyToValue")
    default double moneyToValue(Money money) {
        return money.getValue().doubleValue();
    }
}
