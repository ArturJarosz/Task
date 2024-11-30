package com.arturjarosz.task.supplier.application.mapper;

import com.arturjarosz.task.configuration.UserProperties;
import com.arturjarosz.task.dto.FinancialPartialDataDto;
import com.arturjarosz.task.dto.SupplierSuppliesDataDto;
import com.arturjarosz.task.finance.application.TaxCalculator;
import com.arturjarosz.task.finance.application.dto.FinancialValueDto;
import com.arturjarosz.task.finance.model.SupplierSupplyDataDto;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Set;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {UserProperties.class}, injectionStrategy = InjectionStrategy.CONSTRUCTOR)
public abstract class SupplierSuppliesMapper {

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

    public SupplierSuppliesDataDto mapToSupplierSuppliesDataDto(Set<SupplierSupplyDataDto> supplierSupplyDataDtos) {
        var supplySupplierData = new SupplierSuppliesDataDto();

        supplySupplierData.setSupplies(supplierSupplyDataDtos.stream().map(SupplierSupplyDataDto::supply).toList());

        var financialSummary = new FinancialValueDto();
        for (SupplierSupplyDataDto supplierSupplyDataDto : supplierSupplyDataDtos) {
            var supplyFinancialDetails = supplierSupplyDataDto.financialData();
            var recalculatedSupplyFinancialDetails = TaxCalculator.recalculateObjectTaxes(supplyFinancialDetails,
                    this.userProperties);
            financialSummary.addValues(recalculatedSupplyFinancialDetails);
        }
        var financialSummaryDto = new FinancialPartialDataDto(supplierSupplyDataDtos.size(),
                financialSummary.getNetValue().doubleValue(), financialSummary.getGrossValue().doubleValue(),
                financialSummary.getVatTax().doubleValue(), financialSummary.getIncomeTax().doubleValue());
        financialSummaryDto.setCount(supplierSupplyDataDtos.size());
        supplySupplierData.setFinancialData(financialSummaryDto);

        var averageSummary = calculateAverage(financialSummary, financialSummaryDto.getCount());
        supplySupplierData.setAverageFinancialData(averageSummary);

        return supplySupplierData;
    }
}
