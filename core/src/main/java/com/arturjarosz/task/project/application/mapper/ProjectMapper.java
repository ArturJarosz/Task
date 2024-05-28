package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.dto.ContractDto;
import com.arturjarosz.task.dto.ContractStatusDto;
import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.dto.ProjectStatusDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {StageMapper.class})
public interface ProjectMapper {

    @Mapping(target = "projectWorkflow", source = "projectWorkflow")
    @Mapping(source = "projectCreateDto.name", target = "name")
    @Mapping(source = "contractId", target = "contractId")
    @Mapping(source = "projectCreateDto.type", target = "projectType")
    Project mapFromCreateDto(ProjectCreateDto projectCreateDto, Long contractId,
            ProjectWorkflow projectWorkflow);

    @Mapping(source = "project.projectType", target = "type")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.endDate", target = "endDate")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "project.status", target = "status")
    @Mapping(source = "clientDto.id", target = "client.id")
    @Mapping(source = "clientDto.firstName", target = "client.firstName")
    @Mapping(source = "clientDto.lastName", target = "client.lastName")
    @Mapping(source = "clientDto.companyName", target = "client.companyName")
    @Mapping(source = "clientDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    @Mapping(source = "project", target = "nextStatuses", qualifiedByName = "getNextStatuses")
    @Mapping(source = "contractDto", target = "contract")
    @Mapping(source = "costDtos", target = "costs")
    @Mapping(source = "project.createdDateTime", target = "createdDateTime")
    @Mapping(source = "project.lastModifiedDateTime", target = "lastModifiedDateTime")
    ProjectDto mapToDto(ClientDto clientDto, ArchitectDto architectDto, Project project, ContractDto contractDto,
            List<CostDto> costDtos);

    @Mapping(source = "project.projectType", target = "type")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "project.status", target = "status")
    @Mapping(source = "project.endDate", target = "endDate")
    @Mapping(source = "project", target = "nextStatuses", qualifiedByName = "getNextStatuses")
    @Mapping(source = "contract", target = "contract")
    @Mapping(source = "project.createdDateTime", target = "createdDateTime")
    @Mapping(source = "project.lastModifiedDateTime", target = "lastModifiedDateTime")
    ProjectDto mapToDto(Project project, ContractDto contract);

    @Mapping(source = "projectType", target = "type")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "id", target = "id")
    @Mapping(source = "project", target = "nextStatuses", qualifiedByName = "getNextStatuses")
    ProjectDto mapToDto(Project project);

    @Mapping(source = "project.projectType", target = "type")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "project.startDate", target = "startDate", ignore = true)
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "clientDto.id", target = "client.id", ignore = true)
    @Mapping(source = "clientDto.firstName", target = "client.firstName")
    @Mapping(source = "clientDto.lastName", target = "client.lastName")
    @Mapping(source = "clientDto.companyName", target = "client.companyName")
    @Mapping(source = "clientDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    @Mapping(source = "project.createdDateTime", target = "createdDateTime")
    @Mapping(source = "project.lastModifiedDateTime", target = "lastModifiedDateTime")
    ProjectDto mapToDto(ClientDto clientDto, ArchitectDto architectDto, Project project);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money value) {
        return value.getValue().doubleValue();
    }

    @Named("getNextStatuses")
    default List<ProjectStatusDto> getNextStatuses(Project project) {
        return project.getStatus().getPossibleStatusTransitions().stream()
                .map(status -> ProjectStatusDto.fromValue(status.getStatusName()))
                .toList();
    }

    @Named("getNextContractStatuses")
    default List<ContractStatusDto> getNextContractStatuses(Contract contract) {
        return contract.getStatus().getPossibleStatusTransitions().stream()
                .map(status -> ContractStatusDto.fromValue(status.getStatusName()))
                .toList();
    }
}
