package com.arturjarosz.task.supervision.application.impl;

import com.arturjarosz.task.project.application.ProjectValidator;
import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
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
        LOG.info("Creating supervision.");

        this.supervisionValidator.validateCreateSupervision(supervisionDto);
        this.projectValidator.validateProjectExistence(supervisionDto.getProjectId());
        Supervision supervision = new Supervision(supervisionDto);
        this.supervisionRepository.save(supervision);

        LOG.info("Supervision created.");
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public SupervisionDto updateSupervision(Long supervisionId, SupervisionDto supervisionDto) {
        LOG.info("Updating supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionValidator.validateUpdateSupervision(supervisionDto);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        supervision.update(supervisionDto);

        LOG.info("Supervision with id {} updated.", supervisionId);
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public void deleteSupervision(Long supervisionId) {
        LOG.info("Removing supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionRepository.remove(supervisionId);

        LOG.info("Supervision with id {} removed.", supervisionId);
    }

    @Override
    public SupervisionDto getSupervision(Long supervisionId) {
        LOG.info("Retrieving supervision with id {}.", supervisionId);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        return SupervisionDtoMapper.INSTANCE.supervisionToSupervisionDto(supervision);
    }

    @Transactional
    @Override
    public SupervisionVisitDto createSupervisionVisit(Long supervisionId, SupervisionVisitDto supervisionVisitDto) {
        LOG.info("Creating visit for supervision with id {}.", supervisionId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateCreateSupervisionVisit(supervisionVisitDto);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        SupervisionVisit supervisionVisit = new SupervisionVisit(supervisionVisitDto.getDateOfVisit(),
                supervisionVisitDto.getHoursCount(), supervisionVisitDto.isPayable());
        supervision.addSupervisionVisit(supervisionVisit);
        SupervisionVisitDto createdSupervisionVisitDto = SupervisionVisitDtoMapper.INSTANCE
                .supervisionVisitToSupervisionVisionDto(supervisionVisit);
        createdSupervisionVisitDto.setId(this.getIdForCreatedSupervisionVisit(supervision, supervisionVisit));

        LOG.info("Visit with id {} for supervision with id {} created.", createdSupervisionVisitDto.getId(),
                supervisionId);
        return createdSupervisionVisitDto;
    }

    @Transactional
    @Override
    public SupervisionVisitDto updateSupervisionVisit(Long supervisionId, Long supervisionVisitId,
                                                      SupervisionVisitDto supervisionVisitDto) {
        LOG.info("Updating supervision visit with id {}.", supervisionVisitId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateUpdateSupervisionVisit(supervisionVisitDto);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        Supervision supervision = this.supervisionRepository.load(supervisionId);

        LOG.info("Supervision visit with id {} updated.", supervisionVisitId);
        return SupervisionVisitDtoMapper.INSTANCE.supervisionVisitToSupervisionVisionDto(
                supervision.updateSupervisionVisit(supervisionVisitId, supervisionVisitDto));
    }

    @Override
    public SupervisionVisitDto getSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        LOG.info("Retrieving supervision visit with id {}.", supervisionVisitId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        return this.supervisionQueryService.getSupervisionVisit(supervisionVisitId);
    }

    @Transactional
    @Override
    public void deleteSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        LOG.info("Removing supervision visit with id {}.", supervisionVisitId);

        this.supervisionValidator.validateSupervisionExistence(supervisionId);
        this.supervisionVisitValidator.validateSupervisionHavingSupervisionVisit(supervisionId, supervisionVisitId);
        Supervision supervision = this.supervisionRepository.load(supervisionId);
        supervision.removeSupervisionVisit(supervisionVisitId);

        LOG.info("Supervision visit with id {} removed.", supervisionVisitId);
    }


    private Long getIdForCreatedSupervisionVisit(Supervision supervision, SupervisionVisit supervisionVisit) {
        return supervision.getSupervisionVisits().stream()
                .filter(visit -> visit.equals(supervisionVisit))
                .map(AbstractEntity::getId)
                .findFirst().orElse(null);
    }
}