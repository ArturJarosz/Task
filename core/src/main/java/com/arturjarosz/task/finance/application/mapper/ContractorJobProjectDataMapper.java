package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.common.mapper.MoneyMapper;
import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.dto.ContractorJobProjectDataDto;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import org.mapstruct.Builder;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(uses = {MoneyMapper.class}, builder = @Builder(disableBuilder = true), injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public interface ContractorJobProjectDataMapper {

    @Mapping(target = "financialData.count", source = "contractorJobs", qualifiedByName = "numberOfContractorJobs")
    @Mapping(target = "financialData.netValue", source = "partialData.netValue")
    @Mapping(target = "financialData.grossValue", source = "partialData.grossValue")
    @Mapping(target = "financialData.vatTax", source = "partialData.vatTax")
    @Mapping(target = "financialData.incomeTax", source = "partialData.incomeTax")
    @Mapping(target = "contractorJobs", source = "contractorJobs")
    ContractorJobProjectDataDto map(ProjectFinancialPartialData partialData, List<ContractorJobDto> contractorJobs);

    @Named("numberOfContractorJobs")
    default Integer numberOfContractorJobs(final List<ContractorJobDto> contractorJobs) {
        return contractorJobs.size();
    }
}
