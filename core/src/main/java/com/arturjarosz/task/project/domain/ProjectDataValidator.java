package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import java.time.LocalDate;

public class ProjectDataValidator extends BaseValidator<Project> {
    public static final String VALIDATED_OBJECT = "project";

    public ProjectDataValidator(Project validatedObject) {
        super(validatedObject);
    }

    public void signingDateNotInFuture(LocalDate signingDate) {
        assertIsTrue(!signingDate.isAfter(LocalDate.now()),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID, VALIDATED_OBJECT,
                        ProjectExceptionCodes.SIGNING_DATE));
    }

    public void startDateNotBeforeSigningDate(LocalDate startDate, LocalDate signingDate) {
        assertIsTrue(!startDate.isBefore(signingDate),
                createMessageCode(ExceptionCodes.NOT_VALID, VALIDATED_OBJECT, ProjectExceptionCodes.START_DATE));
    }

    public void endDateNotBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        assertIsTrue(!endDate.isBefore(startDate),
                createMessageCode(ExceptionCodes.NOT_VALID, VALIDATED_OBJECT, ProjectExceptionCodes.END_DATE));
    }

    public void deadlineNotBeforeStartDate(LocalDate startDate, LocalDate deadline) {
        assertIsTrue(!deadline.isBefore(startDate),
                createMessageCode(ExceptionCodes.NOT_VALID, VALIDATED_OBJECT, ProjectExceptionCodes.DEADLINE));
    }

}
