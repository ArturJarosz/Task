package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.ClientValidator;
import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.project.application.ProjectApplicationService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectDtoMapper;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.arturjarosz.task.project.application.ProjectValidator.validateProjectContractDto;
import static com.arturjarosz.task.project.application.ProjectValidator.validateUpdateProjectDto;

@ApplicationService
public class ProjectApplicationServiceImpl implements ProjectApplicationService {

    private final ClientApplicationService clientApplicationService;
    private ClientValidator clientValidator;
    private final ArchitectApplicationService architectApplicationService;
    private final ArchitectValidator architectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectDomainService projectDomainService;
    private final ProjectValidator projectValidator;

    @Autowired
    public ProjectApplicationServiceImpl(ClientApplicationService clientApplicationService,
                                         ClientValidator clientValidator,
                                         ArchitectApplicationService architectApplicationService,
                                         ArchitectValidator architectValidator,
                                         ProjectRepository projectRepository,
                                         ProjectDomainService projectDomainService,
                                         ProjectValidator projectValidator) {

        this.clientApplicationService = clientApplicationService;
        this.clientValidator = clientValidator;
        this.architectApplicationService = architectApplicationService;
        this.architectValidator = architectValidator;
        this.projectRepository = projectRepository;
        this.projectDomainService = projectDomainService;
        this.projectValidator = projectValidator;
    }

    @Transactional
    @Override
    public CreatedEntityDto createProject(ProjectCreateDto projectCreateDto) {
        ProjectValidator.validateProjectBasicDto(projectCreateDto);
        this.architectValidator.validateArchitectExistence(projectCreateDto.getArchitectId());
        Long clientId = projectCreateDto.getClientId();
        this.clientValidator.validateClientExistence(clientId);
        Project project = ProjectDtoMapper.INSTANCE.projectCreateDtoToProject(projectCreateDto);
        project = this.projectRepository.save(project);
        return new CreatedEntityDto(project.getId());
    }

    @Override
    public ProjectDto getProject(Long projectId) {
        Project project = this.projectRepository.load(projectId);
        this.projectValidator.validateProjectExistence(projectId);
        ClientBasicDto clientBasicData = this.clientApplicationService.getClientBasicData(project.getClientId());
        ArchitectDto architectDto = this.architectApplicationService.getArchitect(project.getArchitectId());
        return ProjectDtoMapper.INSTANCE
                .clientArchitectProjectToProjectDto(clientBasicData, architectDto, project);
    }

    @Transactional
    @Override
    public void updateProject(Long projectId, ProjectDto projectDto) {
        //TODO: TA-62 update of the Project should be different to project in different statuses
        Project project = this.projectRepository.load(projectId);
        this.projectValidator.validateProjectExistence(projectId);
        validateUpdateProjectDto(projectDto);
        this.projectDomainService.updateProject(project, projectDto);
        this.projectRepository.save(project);
    }

    @Transactional
    @Override
    public void removeProject(Long projectId) {
        this.projectRepository.remove(projectId);
    }

    @Transactional
    @Override
    public void signProjectContract(Long projectId,
                                    ProjectContractDto projectContractDto) {
        Project project = this.projectRepository.load(projectId);
        this.projectValidator.validateProjectExistence(projectId);
        validateProjectContractDto(projectContractDto);
        this.projectDomainService.signProjectContract(project, projectContractDto);
        this.projectRepository.save(project);
    }

    @Transactional
    @Override
    public void finishProject(Long projectId,
                              ProjectContractDto projectContractDto) {
        //TODO: TA-62 update conditions on that project can be ended
        Project project = this.projectRepository.load(projectId);
        this.projectValidator.validateProjectExistence(projectId);
        this.projectDomainService.finishProject(projectId, projectContractDto.getEndDate());
        this.projectRepository.save(project);
    }

    @Override
    public List<ProjectDto> getProjects() {
        return this.projectRepository.loadAll().stream().map(project -> {
            ClientBasicDto clientBasicData = this.clientApplicationService.getClientBasicData(project.getClientId());
            ArchitectDto architectDto = this.architectApplicationService.getArchitect(project.getArchitectId());
            return ProjectDtoMapper.INSTANCE
                    .clientArchitectProjectToBasicProjectDto(clientBasicData, architectDto, project);
        }).collect(Collectors.toList());
    }
}
