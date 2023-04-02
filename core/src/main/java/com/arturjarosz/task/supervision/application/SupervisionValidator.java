package com.arturjarosz.task.supervision.application;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.model.Supervision;
import com.arturjarosz.task.supervision.query.SupervisionQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.*;

@Component
public class SupervisionValidator {
    private final SupervisionQueryService supervisionQueryService;

    @Autowired
    public SupervisionValidator(SupervisionQueryService supervisionQueryService) {
        this.supervisionQueryService = supervisionQueryService;
    }

    public void validateCreateSupervision(SupervisionDto supervisionDto) {
        this.validateSupervisionDtoNotNull(supervisionDto);
        assertNotNull(supervisionDto.getProjectId(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.PROJECT_ID));
        this.validateSupervisionData(supervisionDto);
    }

    public void validateUpdateSupervision(SupervisionDto supervisionDto) {
        this.validateSupervisionDtoNotNull(supervisionDto);
        this.validateSupervisionData(supervisionDto);
    }

    private void validateSupervisionDtoNotNull(SupervisionDto supervisionDto) {
        assertNotNull(supervisionDto, createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION));
    }

    private void validateSupervisionData(SupervisionDto supervisionDto) {
        assertNotNull(supervisionDto.getHasInvoice(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.INVOICE_FLAG));
        assertNotNull(supervisionDto.getBaseNetRate(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.BASE_NET_RATE));
        assertIsTrue(supervisionDto.getBaseNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.BASE_NET_RATE));
        assertNotNull(supervisionDto.getHourlyNetRate(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.HOURLY_NET_RATE));
        assertIsTrue(supervisionDto.getHourlyNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.HOURLY_NET_RATE));
        assertNotNull(supervisionDto.getVisitNetRate(),
                createMessageCode(ExceptionCodes.NULL, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.VISIT_NET_RATE));
        assertIsTrue(supervisionDto.getVisitNetRate().doubleValue() >= 0.0,
                createMessageCode(ExceptionCodes.NEGATIVE, SupervisionExceptionCodes.SUPERVISION,
                        SupervisionExceptionCodes.VISIT_NET_RATE));
    }

    public void validateSupervisionExistence(Long supervisionId) {
        assertIsTrue(this.supervisionQueryService.supervisionExists(supervisionId),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupervisionExceptionCodes.SUPERVISION), supervisionId);
    }

    public void validateSupervisionExistence(Optional<Supervision> maybeSupervision, Long supervisionId) {
        assertIsTrue(maybeSupervision.isPresent(),
                createMessageCode(ExceptionCodes.NOT_EXIST, SupervisionExceptionCodes.SUPERVISION), supervisionId);
    }

    public void projectNotHavingSupervision(Long projectId) {
        assertIsFalse(
                this.supervisionQueryService.supervisionOnProjectExistence(projectId),
                createMessageCode(ExceptionCodes.ALREADY_SET, ProjectExceptionCodes.PROJECT,
                        SupervisionExceptionCodes.SUPERVISION), projectId);
    }
}
