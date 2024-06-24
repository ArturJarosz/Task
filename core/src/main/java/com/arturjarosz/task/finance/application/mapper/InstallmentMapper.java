package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.finance.model.Installment;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper
public interface InstallmentMapper {

    default Installment mapFromDto(InstallmentDto installmentDto, Long stageId) {
        return new Installment(installmentDto, stageId);
    }

    @Mapping(source = "installment.amount", target = "value", qualifiedByName = "moneyToDouble")
    @Mapping(source = "installment.id", target = "id")
    @Mapping(source = "installment.paid", target = "paid")
    @Mapping(source = "installment.note", target = "note")
    @Mapping(source = "installment.hasInvoice", target = "hasInvoice")
    @Mapping(source = "installment.paymentDate", target = "paymentDate")
    @Mapping(source = "installment.stageId", target = "stageId")
    @Mapping(source = "stageName", target = "stageName")
    InstallmentDto mapToDto(Installment installment, String stageName);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money money) {
        return money.getValue().doubleValue();
    }

    @Named("doubleToMoney")
    default Money doubleToMoney(Double value) {
        return new Money(value);
    }
}
