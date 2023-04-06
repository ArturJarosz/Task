package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectDtoMapper {

    ProjectDtoMapper INSTANCE = Mappers.getMapper(ProjectDtoMapper.class);

    @Mapping(target = "projectWorkflow", source = "projectWorkflow")
    @Mapping(source = "projectCreateDto.name", target = "name")
    @Mapping(source = "contractId", target = "contractId")
    Project projectCreateDtoToProject(ProjectCreateDto projectCreateDto, Long contractId,
            ProjectWorkflow projectWorkflow);

    @Mapping(source = "project.projectType", target = "projectType")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "clientDto.id", target = "client.id")
    @Mapping(source = "clientDto.firstName", target = "client.firstName")
    @Mapping(source = "clientDto.lastName", target = "client.lastName")
    @Mapping(source = "clientDto.companyName", target = "client.companyName")
    @Mapping(source = "clientDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    ProjectDto projectToProjectDto(ClientDto clientDto, ArchitectDto architectDto, Project project);

    @Mapping(source = "project.projectType", target = "projectType")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "contract.id", target = "contractDto.id")
    @Mapping(source = "contract.status", target = "contractDto.contractStatus")
    @Mapping(source = "project.status", target = "status")
    @Mapping(source = "project.endDate", target = "endDate")
    @Mapping(source = "contract.offerValue", target = "contractDto.projectValue", qualifiedByName = "moneyToDouble")
    ProjectDto projectToProjectDto(Project project, Contract contract);

    @Mapping(source = "projectType", target = "projectType")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "id", target = "id")
    ProjectDto projectToProjectDto(Project project);

    @Mapping(source = "project.projectType", target = "projectType")
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
    ProjectDto projectToBasicProjectDto(ClientDto clientDto, ArchitectDto architectDto, Project project);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money value) {
        return value.getValue().doubleValue();
    }
}
