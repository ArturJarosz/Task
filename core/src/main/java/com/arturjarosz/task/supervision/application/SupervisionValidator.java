package com.arturjarosz.task.supervision.application;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class SupervisionValidator {
    private final SupervisionQueryService supervisionQueryService;

    @Autowired
    public SupervisionValidator(SupervisionQueryService supervisionQueryService) {
        this.supervisionQueryService = supervisionQueryService;
    }

    public void validateCreateSupervision(SupervisionDto supervisionDto) {
        assertNotNull(supervisionDto.getProjectId(), ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                SupervisionExceptionCodes.PROJECT_ID);
        this.validateSupervisionData(supervisionDto);
    }

    public void validateUpdateSupervision(SupervisionDto supervisionDto) {
        this.validateSupervisionData(supervisionDto);
    }

    private void validateSupervisionData(SupervisionDto supervisionDto) {
        assertNotNull(supervisionDto.isHasInvoice(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.INVOICE_FLAG));
        assertNotNull(supervisionDto.getBaseNetRate(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.BASE_NET_RATE));
        assertIsTrue(supervisionDto.getBaseNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NOT_VALID, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.BASE_NET_RATE, ExceptionCodes.NEGATIVE));
        assertNotNull(supervisionDto.getHourlyNetRate(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.HOURLY_NET_RATE));
        assertIsTrue(supervisionDto.getHourlyNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.HOURLY_NET_RATE));
        assertNotNull(supervisionDto.getVisitNetRate(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.VISIT_NET_RATE));
        assertIsTrue(supervisionDto.getVisitNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.VISIT_NET_RATE));
    }

    public void validateSupervisionExistence(Long supervisionId) {
        assertNotNull(this.supervisionQueryService.supervisionExists(supervisionId),
                createMessageCode(ExceptionCodes.NOT_EXISTS, SupervisionExceptionCodes.SUPERVISION));
    }
}
