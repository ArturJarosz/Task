package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.common.mapper.MoneyMapper;
import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.CostProjectDataDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {MoneyMapper.class}, builder = @Builder(disableBuilder = true))
public interface CostProjectDataMapper {

    @Mapping(target = "financialData.count", source = "costs", qualifiedByName = "numberOfCosts")
    @Mapping(target = "financialData.netValue", source = "partialData.netValue")
    @Mapping(target = "financialData.grossValue", source = "partialData.grossValue")
    @Mapping(target = "financialData.vatTax", source = "partialData.vatTax")
    @Mapping(target = "financialData.incomeTax", source = "partialData.incomeTax")
    @Mapping(target = "costs", source = "costs")
    CostProjectDataDto map(ProjectFinancialPartialData partialData, List<CostDto> costs);

    @Named("numberOfCosts")
    default Integer numberOfCosts(final List<CostDto> costs) {
        return costs.size();
    }
}
