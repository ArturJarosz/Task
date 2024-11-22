package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.common.mapper.MoneyMapper;
import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.InstallmentProjectDataDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {MoneyMapper.class}, builder = @Builder(disableBuilder = true), injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface InstallmentProjectDataMapper {

    @Mapping(target = "financialData.count", source = "installmentDtos", qualifiedByName = "numberOfInstallments")
    @Mapping(target = "financialData.netValue", source = "partialData.netValue")
    @Mapping(target = "financialData.grossValue", source = "partialData.grossValue")
    @Mapping(target = "financialData.vatTax", source = "partialData.vatTax")
    @Mapping(target = "financialData.incomeTax", source = "partialData.incomeTax")
    @Mapping(target = "installments", source = "installmentDtos")
    @Mapping(target = "stagesWithoutInstallment", source = "stagesWithoutInstallmentIds")
    InstallmentProjectDataDto mapToProjectFinancialPartialDataDto(ProjectFinancialPartialData partialData,
            List<InstallmentDto> installmentDtos, List<Long> stagesWithoutInstallmentIds);

    @Named("numberOfInstallments")
    default Integer numberOfInstallments(final List<InstallmentDto> installmentDtos) {
        return installmentDtos.size();
    }

}
