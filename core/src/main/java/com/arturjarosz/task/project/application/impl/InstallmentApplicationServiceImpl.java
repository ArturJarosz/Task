package com.arturjarosz.task.project.application.impl;

import com.arturjarosz.task.project.application.InstallmentApplicationService;
import com.arturjarosz.task.project.application.InstallmentValidator;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.project.application.mapper.InstallmentDtoMapper;
import com.arturjarosz.task.project.domain.InstallmentDomainService;
import com.arturjarosz.task.project.infrastructure.repositor.ProjectRepository;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.stage.application.StageValidator;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.stage.query.StageQueryService;
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
    private final ProjectRepository projectRepository;
    private final StageValidator stageValidator;
    private final StageQueryService stageQueryService;

    public InstallmentApplicationServiceImpl(InstallmentDomainService installmentDomainService,
                                             ProjectValidator projectValidator, ProjectRepository projectRepository,
                                             StageValidator stageValidator, StageQueryService stageQueryService) {
        this.installmentDomainService = installmentDomainService;
        this.projectValidator = projectValidator;
        this.projectRepository = projectRepository;
        this.stageValidator = stageValidator;
        this.stageQueryService = stageQueryService;
    }

    @Transactional
    @Override
    public InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("creating installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.stageValidator.validateStageNotHavingInstallment(stageId);
        InstallmentValidator.validateCreateInstallmentDto(installmentDto);
        Installment installment = InstallmentDtoMapper.INSTANCE.installmentDtoToInstallment(installmentDto);
        Project project = this.projectRepository.load(projectId);
        project.addInstallmentToStage(stageId, installment);
        project = this.projectRepository.save(project);

        LOG.debug("installment for stage with id {} created", stageId);
        return InstallmentDtoMapper.INSTANCE
                .installmentToInstallmentDto(this.getNewInstallmentWithId(project, stageId));
    }

    @Transactional
    @Override
    public InstallmentDto updateInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("updating installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.stageQueryService.getStageById(stageId);
        Project project = this.projectRepository.load(projectId);
        Installment installment = this.installmentDomainService
                .updateInstallment(stage, installmentDto.getValue(), installmentDto.getPayDate(),
                        installmentDto.getNote());
        LOG.debug("installment for stage with id {} updated", stageId);
        this.projectRepository.save(project);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
    }

    @Transactional
    @Override
    public void removeInstallment(Long projectId, Long stageId) {
        LOG.debug("removing installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.stageQueryService.getStageById(stageId);
        Project project = this.projectRepository.load(projectId);
        stage.removeInstallment();

        LOG.debug("installment for stage with id {} removed", stageId);
        this.projectRepository.save(project);
    }

    @Transactional
    @Override
    public InstallmentDto payInstallment(Long projectId, Long stageId, InstallmentDto installmentDto) {
        LOG.debug("paying for installment");

        this.projectValidator.validateProjectExistence(projectId);
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        Stage stage = this.stageQueryService.getStageById(stageId);
        Project project = this.projectRepository.load(projectId);
        Installment installment = this.installmentDomainService.payForInstallment(stage, installmentDto.getPayDate());

        LOG.debug("payment for installment on stage with id {} made", stageId);
        this.projectRepository.save(project);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(installment);
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
        this.stageValidator.validateExistenceOfStageInProject(projectId, stageId);
        this.stageValidator.validateStageHavingInstallment(stageId);
        Stage stage = this.stageQueryService.getStageById(stageId);
        return InstallmentDtoMapper.INSTANCE.installmentToInstallmentDto(stage.getInstallment());
    }

    private Installment getNewInstallmentWithId(Project project, Long stageId) {
        return project.getStages().stream()
                .filter(stageOnProject -> stageOnProject.getId().equals(stageId)).map(Stage::getInstallment)
                .findFirst().orElse(null);
    }
}
