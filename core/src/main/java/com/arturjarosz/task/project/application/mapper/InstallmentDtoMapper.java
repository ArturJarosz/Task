package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InstallmentDtoMapper {
    InstallmentDtoMapper INSTANCE = Mappers.getMapper(InstallmentDtoMapper.class);

    default Installment installmentDtoToInstallment(InstallmentDto installmentDto) {
        return new Installment(installmentDto.getValue());
    }

    @Mapping(source = "amount", target = "value", qualifiedByName = "moneyToDouble")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "paid", target = "paid")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "paymentDate", target = "payDate")
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
