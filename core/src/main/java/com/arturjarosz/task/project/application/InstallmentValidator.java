package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

import java.time.LocalDate;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

public class InstallmentValidator {

    public static void validateCreateInstallmentDto(InstallmentDto installmentDto) {
        assertNotNull(installmentDto, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.INSTALLMENT));
        assertNotNull(installmentDto.getValue(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.INSTALLMENT,
                        ProjectExceptionCodes.VALUE));
    }

    public static void validatePayDateNotFuture(LocalDate date) {
        assertIsTrue(!date.isAfter(LocalDate.now()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.INSTALLMENT,
                        ProjectExceptionCodes.PAY_DATE));
    }

    public static void validateUpdateInstallmentDto(InstallmentDto installmentDto) {
        assertNotNull(installmentDto, createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.INSTALLMENT));
        assertNotNull(installmentDto.getValue(),
                createMessageCode(ExceptionCodes.IS_NULL, ProjectExceptionCodes.INSTALLMENT,
                        ProjectExceptionCodes.VALUE));
    }
}
