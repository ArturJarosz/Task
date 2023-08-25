package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.finance.model.Installment;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InstallmentDtoMapper {
    InstallmentDtoMapper INSTANCE = Mappers.getMapper(InstallmentDtoMapper.class);

    default Installment installmentDtoToInstallment(InstallmentDto installmentDto, Long stageId) {
        return new Installment(installmentDto, stageId);
    }

    @Mapping(source = "amount", target = "value", qualifiedByName = "moneyToDouble")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "paid", target = "paid")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "hasInvoice", target = "hasInvoice")
    @Mapping(source = "paymentDate", target = "paymentDate")
    @Mapping(source = "stageId", target = "stageId")
    InstallmentDto installmentToInstallmentDto(Installment installment);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money money) {
        return money.getValue().doubleValue();
    }

    @Named("doubleToMoney")
    default Money doubleToMoney(Double value) {
        return new Money(value);
    }
}
