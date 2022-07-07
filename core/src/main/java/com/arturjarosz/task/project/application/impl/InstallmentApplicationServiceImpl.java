package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.InstallmentApplicationService;
import com.arturjarosz.task.project.application.InstallmentValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.project.application.mapper.InstallmentDtoMapper;
import com.arturjarosz.task.project.domain.InstallmentDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@ApplicationService
public class InstallmentApplicationServiceImpl implements InstallmentApplicationService {

    private static final Logger LOG = LoggerFactory.getLogger(InstallmentApplicationServiceImpl.class);

    private final InstallmentDomainService installmentDomainService;
    private final ProjectValidator projectValidator;
    private final ProjectQueryService projectQueryService;
    private final ProjectRepository projectRepository;
    private final StageValidator stageValidator;

    public InstallmentApplicationServiceImpl(InstallmentDomainService installmentDomainService,
            ProjectValidator projectValidator, ProjectQueryService projectQueryService,
            ProjectRepository projectRepository, StageValidator stageValidator) {
        this.installmentDomainService = installmentDomainService;
        this.projectValidator = projectValidator;
        this.projectQueryService = projectQueryService;
        this.projectRepository = projectRepository;
        this.stageValidator = stageValidator;
    }

    @Transactional
    @Override
    public InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("creating installment");

        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.stageValidator.validateStageNotHavingInstallment(stageId);
        InstallmentValidator.validateCreateInstallmentDto(installmentDto);
        Installment installment = InstallmentDtoMapper.INSTANCE.installmentDtoToInstallment(installmentDto);
        Project project = maybeProject.get();
        project.addInstallmentToStage(stageId, installment);
        project = this.projectRepository.save(project);

        LOG.debug("installment for stage with id {} created", stageId);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(
                this.getNewInstallmentWithId(project, stageId));
    }

    @Transactional
    @Override
    public InstallmentDto updateInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("updating installment");

        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        Project project = maybeProject.get();
        Installment installment = this.installmentDomainService.updateInstallment(stage.getInstallment(),
                installmentDto);
        LOG.debug("installment for stage with id {} updated", stageId);
        this.projectRepository.save(project);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
    }

    @Transactional
    @Override
    public void removeInstallment(Long projectId, Long stageId) {
        LOG.debug("removing installment");

        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        Project project = maybeProject.get();
        stage.removeInstallment();

        LOG.debug("installment for stage with id {} removed", stageId);
        this.projectRepository.save(project);
    }

    @Transactional
    @Override
    public InstallmentDto payInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("paying for installment");

        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        Project project = maybeProject.get();
        Installment installment = this.installmentDomainService.payInstallment(stage, installmentDto.getPaymentDate());

        LOG.debug("payment for installment on stage with id {} made", stageId);
        this.projectRepository.save(project);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
    }

    @Override
    public List<InstallmentDto> getInstallmentList(Long projectId) {
        LOG.debug("getting list of installments for project with id {}", projectId);

        Optional<Project> maybeProject = this.projectRepository.findById(projectId);
        this.projectValidator.validateProjectExistence(maybeProject, projectId);
        Project project = maybeProject.get();
        return project.getStages().stream().map(Stage::getInstallment).filter(Objects::nonNull)
                .map(InstallmentDtoMapper.INSTANCE::installmentToInstallmentDto).toList();
    }

    @Override
    public InstallmentDto getInstallment(Long projectId, Long stageId) {
        LOG.debug("getting installment for stage with id {}", stageId);

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.stageValidator.validateStageHavingInstallment(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(stage.getInstallment());
    }

    private Installment getNewInstallmentWithId(Project project, Long stageId) {
        return project.getStages().stream().filter(stageOnProject -> stageOnProject.getId().equals(stageId))
                .map(Stage::getInstallment).findFirst().orElse(null);
    }
}
