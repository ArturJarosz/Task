package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.InstallmentDto;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface InstallmentDtoMapper {
    InstallmentDtoMapper INSTANCE = Mappers.getMapper(InstallmentDtoMapper.class);

    default Installment installmentDtoToInstallment(InstallmentDto installmentDto) {
        return new Installment(installmentDto.getValue());
    }

    default InstallmentDto installmentToInstallmentDto(Installment installment) {
        InstallmentDto installmentDto = new InstallmentDto();
        installmentDto.setPaid(installment.isPaid());
        installmentDto.setValue(installment.getAmount().getValue().doubleValue());
        installmentDto.setDescription(installment.getDescription());
        installmentDto.setPayDate(installment.getPaymentDate());
        return installmentDto;
    }
}
