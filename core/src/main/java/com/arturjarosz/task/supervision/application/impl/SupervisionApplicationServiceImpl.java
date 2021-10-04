package com.arturjarosz.task.supervision.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.supervision.application.SupervisionApplicationService;
import com.arturjarosz.task.supervision.application.SupervisionValidator;
import com.arturjarosz.task.supervision.application.SupervisionVisitValidator;
import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;
import com.arturjarosz.task.supervision.application.mapper.SupervisionDtoMapper;
import com.arturjarosz.task.supervision.application.mapper.SupervisionVisitDtoMapper;
import com.arturjarosz.task.supervision.infrastructure.repository.SupervisionRepository;
import com.arturjarosz.task.supervision.model.Supervision;
import com.arturjarosz.task.supervision.model.SupervisionVisit;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.transaction.Transactional;
import java.math.BigDecimal;

@ApplicationService
public class SupervisionApplicationServiceImpl implements SupervisionApplicationService {
    private static final Logger LOG = LoggerFactory.getLogger(SupervisionApplicationServiceImpl.class);

    private final ProjectValidator projectValidator;
    private final SupervisionValidator supervisionValidator;
    private final SupervisionVisitValidator supervisionVisitValidator;
    private final SupervisionRepository supervisionRepository;
    private final SupervisionQueryService supervisionQueryService;

    @Autowired
    public SupervisionApplicationServiceImpl(ProjectValidator projectValidator,
                                             SupervisionValidator supervisionValidator,
                                             SupervisionVisitValidator supervisionVisitValidator,
                                             SupervisionRepository supervisionRepository,
                                             SupervisionQueryService supervisionQueryService) {
        this.projectValidator = projectValidator;
        this.supervisionValidator = supervisionValidator;
        this.supervisionVisitValidator = supervisionVisitValidator;
        this.supervisionRepository = supervisionRepository;
        this.supervisionQueryService = supervisionQueryService;
    }

    @Transactional
    @Override
    public SupervisionDto createSupervision(SupervisionDto supervisionDto) {
        LOG.debug("Creating supervision.");

        this.supervisionValidator.validateCreateSupervision(supervisionDto);
        this.projectValidator.validateProjectExistence(supervisionDto.getProjectId());
        Supervision supervision = new Supervision(supervisionDto);
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision created.");
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public SupervisionDto updateSupervision(Long supervisionId, SupervisionDto supervisionDto) {
        LOG.debug("Updating supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        supervision.update(supervisionDto);
        this.recalculateSupervisionFinancialData(supervision);
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision with id {} updated.", supervisionId);
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public void deleteSupervision(Long supervisionId) {
        LOG.debug("Removing supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionRepository.remove(supervisionId);

        LOG.debug("Supervision with id {} removed.", supervisionId);
    }

    @Override
    public SupervisionDto getSupervision(Long supervisionId) {
        LOG.debug("Retrieving supervision with id {}.", supervisionId);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public SupervisionVisitDto createSupervisionVisit(Long supervisionId, SupervisionVisitDto supervisionVisitDto) {
        LOG.debug("Creating visit for supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateCreateSupervisionVisit(supervisionVisitDto);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        SupervisionVisit supervisionVisit = new SupervisionVisit(supervisionVisitDto.getDateOfVisit(),
                supervisionVisitDto.getHoursCount(), supervisionVisitDto.getPayable());
        supervision.addSupervisionVisit(supervisionVisit);
        SupervisionVisitDto createdSupervisionVisitDto = SupervisionVisitDtoMapper.INSTANCE
                .supervisionVisitToSupervisionVisionDto(supervisionVisit);
        this.updateSupervisionHoursCount(supervision);
        this.recalculateSupervisionFinancialData(supervision);
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

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateUpdateSupervisionVisit(supervisionVisitDto);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        supervision.updateSupervisionVisit(supervisionVisitId, supervisionVisitDto);
        this.updateSupervisionHoursCount(supervision);
        this.recalculateSupervisionFinancialData(supervision);
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision visit with id {} updated.", supervisionVisitId);
        return SupervisionVisitDtoMapper.INSTANCE.supervisionVisitToSupervisionVisionDto(
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

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        supervision.removeSupervisionVisit(supervisionVisitId);
        this.updateSupervisionHoursCount(supervision);
        this.recalculateSupervisionFinancialData(supervision);
        this.supervisionRepository.save(supervision);

        LOG.debug("Supervision visit with id {} removed.", supervisionVisitId);
    }

    private void recalculateSupervisionFinancialData(Supervision supervision) {
        BigDecimal value = new BigDecimal(0);
        // Adding base rate
        value = value.add(BigDecimal.valueOf(supervision.getBaseNetRate().doubleValue()));
        // Adding hours value and rate per visit
        for (SupervisionVisit supervisionVisit : supervision.getSupervisionVisits()) {
            if (supervisionVisit.isPayable()) {
                BigDecimal hoursValue = BigDecimal.valueOf(
                        supervisionVisit.getHoursCount() * supervision.getHourlyNetRate().doubleValue());
                value = value.add(hoursValue);
                value = value.add(supervision.getVisitNetRate());
            }
        }
        supervision.getFinancialData().setValue(new Money(value));
    }

    private void updateSupervisionHoursCount(Supervision supervision) {
        int hoursCount = supervision.getSupervisionVisits().stream().mapToInt(SupervisionVisit::getHoursCount).sum();
        supervision.setHoursCount(hoursCount);
    }


    private Long getIdForCreatedSupervisionVisit(Supervision supervision, SupervisionVisit supervisionVisit) {
        return supervision.getSupervisionVisits().stream()
                .filter(visit -> visit.equals(supervisionVisit))
                .map(AbstractEntity::getId)
                .findFirst().orElse(null);
    }
}
