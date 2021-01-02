package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.architect.application.ArchitectApplicationService;
import com.arturjarosz.task.architect.application.ArchitectValidator;
import com.arturjarosz.task.architect.application.dto.ArchitectDto;
import com.arturjarosz.task.client.application.ClientApplicationService;
import com.arturjarosz.task.client.application.dto.ClientBasicDto;
import com.arturjarosz.task.client.domain.ClientExceptionCodes;
import com.arturjarosz.task.project.application.ProjectApplicationService;
import com.arturjarosz.task.project.application.ProjectDtoValidator;
import com.arturjarosz.task.project.application.dto.ProjectContractDto;
import com.arturjarosz.task.project.application.dto.ProjectCreateDto;
import com.arturjarosz.task.project.application.dto.ProjectDto;
import com.arturjarosz.task.project.application.mapper.ProjectDtoMapper;
import com.arturjarosz.task.project.domain.ProjectDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

import static com.arturjarosz.task.project.application.ProjectDtoValidator.validateProjectContractDto;
import static com.arturjarosz.task.project.application.ProjectDtoValidator.validateProjectExistence;
import static com.arturjarosz.task.project.application.ProjectDtoValidator.validateUpdateProjectDto;

@ApplicationService
public class ProjectApplicationServiceImpl implements ProjectApplicationService {

    private final ClientApplicationService clientApplicationService;
    private final ArchitectApplicationService architectApplicationService;
    private final ArchitectValidator architectValidator;
    private final ProjectRepository projectRepository;
    private final ProjectDomainService projectDomainService;

    @Autowired
    public ProjectApplicationServiceImpl(ClientApplicationService clientApplicationService,
                                         ArchitectApplicationService architectApplicationService,
                                         ArchitectValidator architectValidator,
                                         ProjectRepository projectRepository,
                                         ProjectDomainService projectDomainService) {

        this.clientApplicationService = clientApplicationService;
        this.architectApplicationService = architectApplicationService;
        this.architectValidator = architectValidator;
        this.projectRepository = projectRepository;
        this.projectDomainService = projectDomainService;
    }

    @Transactional
    @Override
    public CreatedEntityDto createProject(ProjectCreateDto projectCreateDto) {
        ProjectDtoValidator.validateProjectBasicDto(projectCreateDto);
        Long clientId = projectCreateDto.getClientId();
        BaseValidator.assertIsTrue(this.clientApplicationService.getClient(clientId) != null,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXISTS, ClientExceptionCodes.CLIENT), clientId);
        this.architectValidator.validateArchitectExistence(projectCreateDto.getArchitectId());
        Project project = ProjectDtoMapper.INSTANCE.projectCreateDtoToProject(projectCreateDto);
        this.projectRepository.save(project);
        return new CreatedEntityDto(project.getId());
    }

    @Override
    public ProjectDto getProject(Long projectId) {
        Project project = this.projectRepository.load(projectId);
        validateProjectExistence(project, projectId);
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
        validateProjectExistence(project, projectId);
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
    public void singProjectContract(Long projectId,
                                    ProjectContractDto projectContractDto) {
        Project project = this.projectRepository.load(projectId);
        validateProjectExistence(project, projectId);
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
        validateProjectExistence(project, projectId);
        project.finishProject(projectContractDto.getEndDate());
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
