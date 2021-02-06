package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.InstallmentApplicationService;
import com.arturjarosz.task.project.application.InstallmentValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.StageValidator;
import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.project.domain.InstallmentDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.InstallmentDtoMapper;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.CreatedEntityDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
    public CreatedEntityDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("creating installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        InstallmentValidator.validateCreateInstallmentDto(installmentDto);
        Installment installment = InstallmentDtoMapper.INSTANCE.installmentDtoToInstallment(installmentDto);
        this.stageValidator.validateStageNotHavingInstallment(stageId);
        Project project = this.projectRepository.load(projectId);
        stage.setInstallment(installment);
        project = this.projectRepository.save(project);

        LOG.debug("installment for stage with id {} created", stageId);
        return new CreatedEntityDto(this.getIdForInstallmentInStage(project, stageId));
    }

    @Transactional
    @Override
    public void updateInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("updating installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        Project project = this.projectRepository.load(projectId);
        this.installmentDomainService.updateInstallment(stage, installmentDto.getValue(), installmentDto.getPayDate(),
                installmentDto.getDescription());

        LOG.debug("installment for stage with id {} updated", stageId);
        this.projectRepository.save(project);
    }

    @Transactional
    @Override
    public void removeInstallment(Long projectId, Long stageId) {
        LOG.debug("removing installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        Project project = this.projectRepository.load(projectId);
        stage.removeInstallment();

        LOG.debug("installment for stage with id {} removed", stageId);
        this.projectRepository.save(project);
    }

    @Override
    public void payInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("paying for installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        Project project = this.projectRepository.load(projectId);
        this.installmentDomainService.payForInstallment(stage, installmentDto.getPayDate());

        LOG.debug("payment for installment on stage with id {} made", stageId);
        this.projectRepository.save(project);
    }

    @Override
    public List<InstallmentDto> getInstallmentList(Long projectId) {
        LOG.debug("getting list of installments for project with id {}", projectId);

        this.projectValidator.validateProjectExistence(projectId);
        Project project = this.projectRepository.load(projectId);
        return project.getStages().stream()
                .map(Stage::getInstallment)
                .filter(Objects::nonNull)
                .map(InstallmentDtoMapper.INSTANCE::installmentToInstallmentDto)
                .collect(Collectors.toList());
    }

    @Override
    public InstallmentDto getInstallment(Long projectId, Long stageId) {
        LOG.debug("getting installment for stage with id {}", stageId);

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateStageExistence(stageId);
        this.stageValidator.validateStageHavingInstallment(stageId);
        Stage stage = this.projectQueryService.getStageById(stageId);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(stage.getInstallment());
    }

    private Long getIdForInstallmentInStage(Project project, Long stageId) {
        return project.getStages()
                .stream()
                .filter(stageInProject -> stageInProject.getId().equals(stageId))
                .map(stage -> stage.getInstallment().getId()).findFirst().orElseThrow();
    }
}
