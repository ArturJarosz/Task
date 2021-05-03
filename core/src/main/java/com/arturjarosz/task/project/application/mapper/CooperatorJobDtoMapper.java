package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.CooperatorJobType;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface CooperatorJobDtoMapper {
    CooperatorJobDtoMapper INSTANCE = Mappers.getMapper(CooperatorJobDtoMapper.class);

    default CooperatorJob contractorJobCreateDtoToCooperatorJob(ContractorJobDto contractorJobDto) {
        CooperatorJob cooperatorJob = new CooperatorJob(contractorJobDto.getName(), contractorJobDto.getContractorId(),
                CooperatorJobType.CONTRACTOR_JOB);
        cooperatorJob.setValue(contractorJobDto.getValue());
        return cooperatorJob;
    }

    @Mapping(source = "value", target = "value", qualifiedByName = "moneyToDouble")
    ContractorJobDto cooperatorJobToContractorJobDto(CooperatorJob cooperatorJob);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money money) {
        return money.getValue().doubleValue();
    }
}
