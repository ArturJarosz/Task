package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.dto.ArchitectDto;
import com.arturjarosz.task.dto.ClientDto;
import com.arturjarosz.task.dto.ProjectCreateDto;
import com.arturjarosz.task.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.model.Money;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectDtoMapper {

    ProjectDtoMapper INSTANCE = Mappers.getMapper(ProjectDtoMapper.class);

    @Mapping(target = "projectWorkflow", source = "projectWorkflow")
    @Mapping(source = "projectCreateDto.name", target = "name")
    @Mapping(source = "contractId", target = "contractId")
    @Mapping(source = "projectCreateDto.type", target = "projectType")
    Project projectCreateDtoToProject(ProjectCreateDto projectCreateDto, Long contractId,
            ProjectWorkflow projectWorkflow);

    @Mapping(source = "project.projectType", target = "type")
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

    @Mapping(source = "project.projectType", target = "type")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "contract.id", target = "contract.id")
    @Mapping(source = "contract.status", target = "contract.status")
    @Mapping(source = "project.status", target = "status")
    @Mapping(source = "project.endDate", target = "endDate")
    @Mapping(source = "contract.offerValue", target = "contract.projectValue", qualifiedByName = "moneyToDouble")
    ProjectDto projectToProjectDto(Project project, Contract contract);

    @Mapping(source = "projectType", target = "type")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "startDate", target = "startDate")
    @Mapping(source = "note", target = "note")
    @Mapping(source = "id", target = "id")
    ProjectDto projectToProjectDto(Project project);

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
    ProjectDto projectToBasicProjectDto(ClientDto clientDto, ArchitectDto architectDto, Project project);

    @Named("moneyToDouble")
    default Double moneyToDouble(Money value) {
        return value.getValue().doubleValue();
    }
}
