package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.PeriodTypeDto;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@Component
public class FinancialReportValidator {

    public void validateFinancialReportParameters(LocalDate startDate, LocalDate endDate, PeriodTypeDto periodType) {
        assertNotNull(startDate,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.FINANCIAL_REPORT,
                        ProjectExceptionCodes.START_DATE));
        assertNotNull(endDate,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.FINANCIAL_REPORT,
                        ProjectExceptionCodes.END_DATE));
        assertNotNull(periodType,
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.FINANCIAL_REPORT,
                        ProjectExceptionCodes.PERIOD_TYPE));
        assertIsTrue(!startDate.isAfter(endDate),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.FINANCIAL_REPORT,
                        ProjectExceptionCodes.START_DATE));
    }
}
