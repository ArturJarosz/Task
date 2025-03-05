package com.arturjarosz.task.contractor.application.mapper;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.dto.ContractorContractorJobsDataDto;
import com.arturjarosz.task.dto.FinancialPartialDataDto;
import com.arturjarosz.task.finance.application.TaxCalculator;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.model.ContractorContractorJobDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Mapper(uses = {UserProperties.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class ContractorContractorJobMapper {

    @Autowired
    private UserProperties userProperties;

    private static FinancialPartialDataDto calculateAverage(FinancialValueDto financialSummary, int count) {
        var averageSummary = new FinancialPartialDataDto();
        if (count > 0) {
            averageSummary.setNetValue(financialSummary.getNetValue()
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    .doubleValue());
            averageSummary.setGrossValue(financialSummary.getGrossValue()
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    .doubleValue());
            averageSummary.setVatTax(financialSummary.getVatTax()
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    .doubleValue());
            averageSummary.setIncomeTax(financialSummary.getIncomeTax()
                    .divide(BigDecimal.valueOf(count), 2, RoundingMode.HALF_UP)
                    .doubleValue());
        } else {
            averageSummary.setNetValue(0.0);
            averageSummary.setGrossValue(0.0);
            averageSummary.setIncomeTax(0.0);
            averageSummary.setVatTax(0.0);
        }
        return averageSummary;
    }

    public ContractorContractorJobsDataDto mapToContractorContractorJobsDataDto(
            Set<ContractorContractorJobDto> contractorContractorJobDtos) {
        var contractorContractorJobsDataDto = new ContractorContractorJobsDataDto();

        contractorContractorJobsDataDto.setContractorJobs(
                contractorContractorJobDtos.stream().map(ContractorContractorJobDto::contractorJob).toList());

        var financialSummary = new FinancialValueDto();
        for (ContractorContractorJobDto contractorJobsDataDto : contractorContractorJobDtos) {
            var contractorJobFinancialDetails = contractorJobsDataDto.financialData();
            var recalculatedSupplyFinancialDetails = TaxCalculator.recalculateObjectTaxes(contractorJobFinancialDetails,
                    this.userProperties);
            financialSummary.addValues(recalculatedSupplyFinancialDetails);
        }
        var financialSummaryDto = new FinancialPartialDataDto(contractorContractorJobDtos.size(),
                financialSummary.getNetValue().doubleValue(), financialSummary.getGrossValue().doubleValue(),
                financialSummary.getVatTax().doubleValue(), financialSummary.getIncomeTax().doubleValue());
        financialSummaryDto.setCount(contractorContractorJobDtos.size());
        contractorContractorJobsDataDto.setFinancialData(financialSummaryDto);

        var averageSummary = calculateAverage(financialSummary, financialSummaryDto.getCount());
        contractorContractorJobsDataDto.setAverageFinancialData(averageSummary);

        return contractorContractorJobsDataDto;
    }
}
