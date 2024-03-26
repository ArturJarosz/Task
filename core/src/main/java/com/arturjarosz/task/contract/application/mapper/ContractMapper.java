package com.arturjarosz.task.contract.application.mapper;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.dto.ContractStatusDto;
import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ContractMapper {

    @Mapping(source = "offerValue", target = "offerValue")
    @Mapping(source = "deadline", target = "deadline")
    ContractDto projectDtoToContractDto(ProjectCreateDto projectCreateDto);

    @Mapping(source = "signingDate", target = "signingDate")
    @Mapping(source = "deadline", target = "deadline")
    @Mapping(source = "status", target = "status")
    @Mapping(target = "offerValue", source = "offerValue", qualifiedByName = "moneyToDouble")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "endDate", target = "endDate")
    @Mapping(source = "contract", target = "nextStatuses", qualifiedByName = "getNextStatuses")
    ContractDto mapToDto(Contract contract);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money value) {
        return value.getValue().doubleValue();
    }

    @Named("getNextStatuses")
    default List<ContractStatusDto> getNextStatuses(Contract contract) {
        return contract.getStatus().getPossibleStatusTransitions().stream()
                .map(status -> ContractStatusDto.fromValue(status.getStatusName()))
                .toList();
    }
}
