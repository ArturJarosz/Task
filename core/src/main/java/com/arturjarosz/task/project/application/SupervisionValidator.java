package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class SupervisionValidator {

    public void validateCreateSupervision(SupervisionDto supervisionDto) {
        this.validateSupervisionData(supervisionDto);
    }

    public void validateUpdateSupervision(SupervisionDto supervisionDto) {
        this.validateSupervisionData(supervisionDto);
    }

    private void validateSupervisionData(SupervisionDto supervisionDto) {
        assertNotNull(supervisionDto.isHasInvoice(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.SUPERVISION,
                        ProjectExceptionCodes.INVOICE_FLAG));
        assertNotNull(supervisionDto.getBaseNetRate(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.BASE_NET_RATE));
        assertIsTrue(supervisionDto.getBaseNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.SUPERVISION,
                        ProjectExceptionCodes.BASE_NET_RATE, ProjectExceptionCodes.NEGATIVE));
        assertNotNull(supervisionDto.getHourlyNetRate(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.HOURLY_NET_RATE));
        assertIsTrue(supervisionDto.getHourlyNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.SUPERVISION,
                        ProjectExceptionCodes.HOURLY_NET_RATE, ProjectExceptionCodes.NEGATIVE));
        assertNotNull(supervisionDto.getVisitNetRate(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.VISIT_NET_RATE));
        assertIsTrue(supervisionDto.getVisitNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.SUPERVISION,
                        ProjectExceptionCodes.VISIT_NET_RATE, ProjectExceptionCodes.NEGATIVE));
    }
}
