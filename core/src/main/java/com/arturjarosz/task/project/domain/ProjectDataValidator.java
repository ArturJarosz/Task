package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class ProjectDataValidator {
    public static final String VALIDATED_OBJECT = "project";

    public ProjectDataValidator() {
        // needed by JPA
    }

    public void startDatePresent(LocalDate startDate) {
        assertNotNull(startDate,
                createMessageCode(ExceptionCodes.NULL, VALIDATED_OBJECT, ProjectExceptionCodes.START_DATE));
    }

    public void endDateNotBeforeStartDate(LocalDate startDate, LocalDate endDate) {
        assertIsTrue(!endDate.isBefore(startDate),
                createMessageCode(ExceptionCodes.NOT_VALID, VALIDATED_OBJECT, ProjectExceptionCodes.START_DATE,
                        ProjectExceptionCodes.END_DATE));
    }
}
