package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ProjectDtoMapper {

    ProjectDtoMapper INSTANCE = Mappers.getMapper(ProjectDtoMapper.class);

    @Mapping(target = "projectWorkflow", source = "projectWorkflow")
    @Mapping(source = "projectCreateDto.name", target = "name")
    Project projectCreateDtoToProject(ProjectCreateDto projectCreateDto, ProjectWorkflow projectWorkflow);

    @Mapping(source = "projectType", target = "projectType")
    @Mapping(source = "name", target = "name")
    @Mapping(source = "clientId", target = "clientId")
    @Mapping(source = "architectId", target = "architectId")
    ProjectCreateDto projectToProjectBasicDto(Project project);

    @Mapping(source = "project.projectType", target = "projectType")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.signingDate", target = "signingDate")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.deadline", target = "deadline")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "clientBasicDto.id", target = "client.id", ignore = true)
    @Mapping(source = "clientBasicDto.firstName", target = "client.firstName")
    @Mapping(source = "clientBasicDto.lastName", target = "client.lastName")
    @Mapping(source = "clientBasicDto.companyName", target = "client.companyName")
    @Mapping(source = "clientBasicDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    ProjectDto clientArchitectProjectToProjectDto(ClientBasicDto clientBasicDto, ArchitectDto architectDto,
                                                  Project project);

    @Mapping(source = "project.projectType", target = "projectType")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.signingDate", target = "signingDate", ignore = true)
    @Mapping(source = "project.startDate", target = "startDate", ignore = true)
    @Mapping(source = "project.deadline", target = "deadline", ignore = true)
    @Mapping(source = "clientBasicDto.id", target = "client.id", ignore = true)
    @Mapping(source = "clientBasicDto.firstName", target = "client.firstName")
    @Mapping(source = "clientBasicDto.lastName", target = "client.lastName")
    @Mapping(source = "clientBasicDto.companyName", target = "client.companyName")
    @Mapping(source = "clientBasicDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    ProjectDto clientArchitectProjectToBasicProjectDto(ClientBasicDto clientBasicDto, ArchitectDto architectDto,
                                                       Project project);

}
