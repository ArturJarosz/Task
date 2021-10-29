package com.arturjarosz.task.supervision.application;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class SupervisionVisitValidator {
    private final SupervisionQueryService supervisionQueryService;

    @Autowired
    public SupervisionVisitValidator(SupervisionQueryService supervisionQueryService) {
        this.supervisionQueryService = supervisionQueryService;
    }

    public void validateCreateSupervisionVisit(SupervisionVisitDto supervisionVisitDto) {
        this.validateSupervisionVisitData(supervisionVisitDto);
    }

    public void validateUpdateSupervisionVisit(SupervisionVisitDto supervisionVisitDto) {
        this.validateSupervisionVisitData(supervisionVisitDto);
    }

    private void validateSupervisionVisitData(SupervisionVisitDto supervisionVisitDto) {
        assertNotNull(supervisionVisitDto,
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION_VISIT));
        assertNotNull(supervisionVisitDto.getPayable(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION_VISIT,
                        SupervisionExceptionCodes.PAYABLE_FLAG));
        assertNotNull(supervisionVisitDto.getDateOfVisit(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION_VISIT,
                        SupervisionExceptionCodes.DATE_OF_VISIT));
        assertNotNull(supervisionVisitDto.getHoursCount(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION_VISIT,
                        SupervisionExceptionCodes.HOURS_COUNT));
        assertIsTrue(supervisionVisitDto.getHoursCount() >= 0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupervisionExceptionCodes.SUPERVISION_VISIT,
                        SupervisionExceptionCodes.HOURS_COUNT));
    }

    public void validateSupervisionHavingSupervisionVisit(Long supervisionId, Long supervisionVisitId) {
        assertNotNull(
                this.supervisionQueryService.supervisionVisitExistsInSupervision(supervisionId, supervisionVisitId),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.SUPERVISION_VISIT));
    }
}
