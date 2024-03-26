package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.WARN)
public interface FinancialDataMapper {

    @Mapping(source = "value", target = "value", qualifiedByName = "moneyToDouble")
    FinancialDataDto map(final FinancialData financialData);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money money) {
        return money.getValue().doubleValue();
    }
}
