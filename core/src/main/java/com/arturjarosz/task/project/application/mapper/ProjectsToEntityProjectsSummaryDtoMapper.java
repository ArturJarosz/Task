package com.arturjarosz.task.project.application.mapper;

import com.arturjarosz.task.dto.EntityProjectsSummaryDto;
import com.arturjarosz.task.dto.ProjectSummaryDto;
import com.arturjarosz.task.project.model.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Mapper()
public interface ProjectsToEntityProjectsSummaryDtoMapper {

    default EntityProjectsSummaryDto mapToProjectSummaryDto(List<Project> projects,
            Map<Long, BigDecimal> contractValueByProjectId) {
        var projectsSummaryDto = new EntityProjectsSummaryDto();
        projectsSummaryDto.setNumberOfProjects(projects.size());
        projectsSummaryDto.setTotalValue(
                contractValueByProjectId.values().stream().reduce(BigDecimal.ZERO, BigDecimal::add).doubleValue());

        projectsSummaryDto.setProjects(projects.stream().map(this::mapProjectToProjectSummaryDto).toList());
        projectsSummaryDto.getProjects()
                .forEach(projectSummaryDto -> projectSummaryDto.setValue(
                        contractValueByProjectId.get(projectSummaryDto.getId()).doubleValue()));
        return projectsSummaryDto;
    }

    @Mapping(source = "name", target = "name")
    @Mapping(source = "projectType", target = "type")
    ProjectSummaryDto mapProjectToProjectSummaryDto(Project project);
}
