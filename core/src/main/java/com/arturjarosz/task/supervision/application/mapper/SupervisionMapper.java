package com.arturjarosz.task.supervision.application.mapper;

import com.arturjarosz.task.dto.SupervisionDto;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.supervision.model.Supervision;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.math.BigDecimal;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface SupervisionMapper {

    @Mapping(target = "hasInvoice", source = "financialData.hasInvoice")
    @Mapping(target = "value", source = "financialData.value", qualifiedByName = "moneyToBigDecimal")
    @Mapping(target = "visitCount", expression = "java(supervision.getSupervisionVisits() != null ? supervision.getSupervisionVisits().size() : 0)")
    SupervisionDto mapToDto(Supervision supervision);

    @Named("moneyToBigDecimal")
    default BigDecimal moneyToBigDecimal(Money money) {
        return money.getValue();
    }

}
