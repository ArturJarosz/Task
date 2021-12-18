package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.client.application.dto.ClientDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.model.Arrangement;
import com.arturjarosz.task.project.model.Offer;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
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
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "clientDto.id", target = "client.id")
    @Mapping(source = "clientDto.firstName", target = "client.firstName")
    @Mapping(source = "clientDto.lastName", target = "client.lastName")
    @Mapping(source = "clientDto.companyName", target = "client.companyName")
    @Mapping(source = "clientDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    @Mapping(source = "project.arrangement", target = "projectValue", qualifiedByName = "getOfferValue")
    @Mapping(source = "project.arrangement", target = "offerAccepted", qualifiedByName = "isOfferAccepted")
    ProjectDto projectToProjectDto(ClientDto clientDto, ArchitectDto architectDto, Project project);

    @Mapping(source = "project.projectType", target = "projectType")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.signingDate", target = "signingDate")
    @Mapping(source = "project.startDate", target = "startDate")
    @Mapping(source = "project.deadline", target = "deadline")
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "project.arrangement", target = "projectValue", qualifiedByName = "getOfferValue")
    @Mapping(source = "project.arrangement", target = "offerAccepted", qualifiedByName = "isOfferAccepted")
    ProjectDto projectToProjectDto(Project project);

    @Mapping(source = "project.projectType", target = "projectType")
    @Mapping(source = "project.name", target = "name")
    @Mapping(source = "project.id", target = "id")
    @Mapping(source = "project.signingDate", target = "signingDate", ignore = true)
    @Mapping(source = "project.startDate", target = "startDate", ignore = true)
    @Mapping(source = "project.deadline", target = "deadline", ignore = true)
    @Mapping(source = "project.note", target = "note")
    @Mapping(source = "clientDto.id", target = "client.id", ignore = true)
    @Mapping(source = "clientDto.firstName", target = "client.firstName")
    @Mapping(source = "clientDto.lastName", target = "client.lastName")
    @Mapping(source = "clientDto.companyName", target = "client.companyName")
    @Mapping(source = "clientDto.clientType", target = "client.clientType")
    @Mapping(source = "architectDto", target = "architect")
    ProjectDto projectToBasicProjectDto(ClientDto clientDto, ArchitectDto architectDto, Project project);

    @Named("getOfferValue")
    default double getOfferValue(Arrangement arrangement) {
        return ((Offer) arrangement).getOfferValue().getValue().doubleValue();
    }

    @Named("isOfferAccepted")
    default boolean isOfferAccepted(Arrangement arrangement) {
        return ((Offer) arrangement).isAccepted();
    }
}
