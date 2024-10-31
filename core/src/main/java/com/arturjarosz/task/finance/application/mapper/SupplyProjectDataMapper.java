package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.common.mapper.MoneyMapper;
import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.SupplyProjectDataDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {MoneyMapper.class}, builder = @Builder(disableBuilder = true))
public interface SupplyProjectDataMapper {

    @Mapping(target = "financialData.count", source = "supplyDtos", qualifiedByName = "numberOfSupplies")
    @Mapping(target = "financialData.netValue", source = "partialData.netValue")
    @Mapping(target = "financialData.grossValue", source = "partialData.grossValue")
    @Mapping(target = "financialData.vatTax", source = "partialData.vatTax")
    @Mapping(target = "financialData.incomeTax", source = "partialData.incomeTax")
    @Mapping(target = "supplies", source = "supplyDtos")
    SupplyProjectDataDto map(ProjectFinancialPartialData partialData,
            List<SupplyDto> supplyDtos);

    @Named("numberOfSupplies")
    default Integer numberOfSupplies(final List<SupplyDto> supplyDtos) {
        return supplyDtos.size();
    }
}
