package com.arturjarosz.task.finance.application.mapper;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.finance.model.ContractorJob;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper
public interface ContractorJobMapper {

    default ContractorJob mapFromDto(ContractorJobDto contractorJobDto) {
        return new ContractorJob(contractorJobDto.getName(), contractorJobDto.getContractorId(),
                contractorJobDto.getValue(), contractorJobDto.getHasInvoice(), contractorJobDto.getPayable(),
                contractorJobDto.getPaid(), contractorJobDto.getPaymentDate());
    }

    @Mapping(source = "contractorJob.financialData.paymentDate", target = "paymentDate")
    ContractorJobDto mapToDto(ContractorJob contractorJob, Long projectId);
}
