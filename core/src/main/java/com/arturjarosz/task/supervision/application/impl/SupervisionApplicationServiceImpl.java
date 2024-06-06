package com.arturjarosz.task.supervision.application.impl;

import com.arturjarosz.task.dto.SupervisionDto;
import com.arturjarosz.task.dto.SupervisionVisitDto;
import com.arturjarosz.task.finance.application.ProjectFinanceAwareObjectService;
import com.arturjarosz.task.finance.application.ProjectFinancialDataService;
import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.ResourceNotFoundException;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.supervision.application.SupervisionApplicationService;
import com.arturjarosz.task.supervision.application.SupervisionValidator;
import com.arturjarosz.task.supervision.application.SupervisionVisitValidator;
import com.arturjarosz.task.supervision.application.mapper.SupervisionMapper;
import com.arturjarosz.task.supervision.application.mapper.SupervisionVisitMapper;
import com.arturjarosz.task.supervision.infrastructure.repository.SupervisionRepository;
import com.arturjarosz.task.supervision.model.Supervision;
import com.arturjarosz.task.supervision.model.SupervisionVisit;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@ApplicationService
public class SupervisionApplicationServiceImpl implements SupervisionApplicationService {

    @NonNull
    private final ProjectValidator projectValidator;
    @NonNull
    private final SupervisionValidator supervisionValidator;
    @NonNull
    private final SupervisionVisitValidator supervisionVisitValidator;
    @NonNull
    private final SupervisionRepository supervisionRepository;
    @NonNull
    private final SupervisionQueryService supervisionQueryService;
    @NonNull
    private final ProjectFinancialDataService projectFinancialSummaryApplicationService;
    @NonNull
    private final ProjectFinanceAwareObjectService projectFinanceAwareObjectService;
    @NonNull
    private final SupervisionMapper supervisionMapper;
    @NonNull
    private final SupervisionVisitMapper supervisionVisitMapper;

    @Transactional
    @Override
    public SupervisionDto createSupervision(SupervisionDto supervisionDto) {
        LOG.debug("Creating supervision.");

        this.supervisionValidator.validateCreateSupervision(supervisionDto);
        this.projectValidator.validateProjectExistence(supervisionDto.getProjectId());
        this.supervisionValidator.projectNotHavingSupervision(supervisionDto.getProjectId());
        var supervision = new Supervision(supervisionDto);
        this.supervisionRepository.save(supervision);
        this.projectFinanceAwareObjectService.onCreate(supervisionDto.getProjectId());

        LOG.debug("Supervision created.");
        return this.supervisionMapper.mapToDto(supervision);
    }

    @Transactional
    @Override
    public SupervisionDto updateSupervision(Long supervisionId, SupervisionDto supervisionDto) {
        LOG.debug("Updating supervision with id {}.", supervisionId);

        var maybeSupervision = this.supervisionRepository.findById(supervisionId);
        this.supervisionValidator.validateSupervisionExistence(maybeSupervision, supervisionId);
        this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        var supervision = maybeSupervision.orElseThrow(ResourceNotFoundException::new);
        supervision.update(supervisionDto);
        this.projectFinancialSummaryApplicationService.recalculateSupervision(supervisionId,
                supervision.getFinancialData().getId());
        this.projectFinanceAwareObjectService.onUpdate(supervisionDto.getProjectId());
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision with id {} updated.", supervisionId);
        return this.supervisionMapper.mapToDto(supervision);
    }

    @Transactional
    @Override
    public void deleteSupervision(Long supervisionId) {
        LOG.debug("Removing supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.projectFinanceAwareObjectService.onRemove(
                this.supervisionQueryService.getProjectIdForSupervision(supervisionId));
        this.supervisionRepository.deleteById(supervisionId);

        LOG.debug("Supervision with id {} removed.", supervisionId);
    }

    @Override
    public SupervisionDto getSupervision(Long supervisionId) {
        LOG.debug("Retrieving supervision with id {}.", supervisionId);
        var maybeSupervision = this.supervisionRepository.findById(supervisionId);
        this.supervisionValidator.validateSupervisionExistence(maybeSupervision, supervisionId);
        return this.supervisionMapper.mapToDto(
                maybeSupervision.orElseThrow(ResourceNotFoundException::new));
    }

    @Transactional
    @Override
    public SupervisionVisitDto createSupervisionVisit(Long supervisionId, SupervisionVisitDto supervisionVisitDto) {
        LOG.debug("Creating visit for supervision with id {}.", supervisionId);

        var maybeSupervision = this.supervisionRepository.findById(supervisionId);
        this.supervisionValidator.validateSupervisionExistence(maybeSupervision, supervisionId);
        this.supervisionVisitValidator.validateCreateSupervisionVisit(supervisionVisitDto);
        var supervision = maybeSupervision.orElseThrow(ResourceNotFoundException::new);
        var supervisionVisit = new SupervisionVisit(supervisionVisitDto.getDateOfVisit(),
                supervisionVisitDto.getHoursCount(), supervisionVisitDto.getPayable());
        supervision.addSupervisionVisit(supervisionVisit);
        var createdSupervisionVisitDto = this.supervisionVisitMapper.mapToDto(
                supervisionVisit);
        this.updateSupervisionHoursCount(supervision);
        this.projectFinancialSummaryApplicationService.recalculateSupervision(supervisionId,
                supervision.getFinancialData().getId());
        this.projectFinanceAwareObjectService.onCreate(
                this.supervisionQueryService.getProjectIdForSupervision(supervisionId));
        this.supervisionRepository.save(supervision);
        createdSupervisionVisitDto.setId(this.getIdForCreatedSupervisionVisit(supervision, supervisionVisit));
        createdSupervisionVisitDto.setSupervisionId(supervisionId);
        LOG.debug("Visit with id {} for supervision with id {} created.", createdSupervisionVisitDto.getId(),
                supervisionId);
        return createdSupervisionVisitDto;
    }

    @Transactional
    @Override
    public SupervisionVisitDto updateSupervisionVisit(Long supervisionId, Long supervisionVisitId,
            SupervisionVisitDto supervisionVisitDto) {
        LOG.debug("Updating supervision visit with id {}.", supervisionVisitId);

        var maybeSupervision = this.supervisionRepository.findById(supervisionId);
        this.supervisionValidator.validateSupervisionExistence(maybeSupervision, supervisionId);
        this.supervisionVisitValidator.validateUpdateSupervisionVisit(supervisionVisitDto);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        var supervision = maybeSupervision.orElseThrow(ResourceNotFoundException::new);
        supervision.updateSupervisionVisit(supervisionVisitId, supervisionVisitDto);
        this.updateSupervisionHoursCount(supervision);
        this.projectFinancialSummaryApplicationService.recalculateSupervision(supervisionId,
                supervision.getFinancialData().getId());
        this.projectFinanceAwareObjectService.onUpdate(
                this.supervisionQueryService.getProjectIdForSupervision(supervisionId));
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision visit with id {} updated.", supervisionVisitId);
        return this.supervisionVisitMapper.mapToDto(
                supervision.updateSupervisionVisit(supervisionVisitId, supervisionVisitDto));
    }

    @Override
    public SupervisionVisitDto getSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        LOG.debug("Retrieving supervision visit with id {}.", supervisionVisitId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        return this.supervisionQueryService.getSupervisionVisit(supervisionVisitId);
    }

    @Transactional
    @Override
    public void deleteSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        LOG.debug("Removing supervision visit with id {}.", supervisionVisitId);

        var maybeSupervision = this.supervisionRepository.findById(supervisionId);
        this.supervisionValidator.validateSupervisionExistence(maybeSupervision, supervisionId);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        var supervision = maybeSupervision.orElseThrow(ResourceNotFoundException::new);
        supervision.removeSupervisionVisit(supervisionVisitId);
        this.updateSupervisionHoursCount(supervision);
        this.projectFinancialSummaryApplicationService.recalculateSupervision(supervisionId,
                supervision.getFinancialData().getId());
        this.projectFinanceAwareObjectService.onRemove(
                this.supervisionQueryService.getProjectIdForSupervision(supervisionId));
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision visit with id {} removed.", supervisionVisitId);
    }

    private void updateSupervisionHoursCount(Supervision supervision) {
        int hoursCount = supervision.getSupervisionVisits().stream().mapToInt(SupervisionVisit::getHoursCount).sum();
        supervision.setHoursCount(hoursCount);
    }


    private Long getIdForCreatedSupervisionVisit(Supervision supervision, SupervisionVisit supervisionVisit) {
        return supervision.getSupervisionVisits()
                .stream()
                .filter(visit -> visit.equals(supervisionVisit))
                .map(AbstractEntity::getId)
                .findFirst()
                .orElseThrow(ResourceNotFoundException::new);
    }
}
