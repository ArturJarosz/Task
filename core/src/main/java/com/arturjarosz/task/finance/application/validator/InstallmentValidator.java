package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

@RequiredArgsConstructor
@Component
public class InstallmentValidator {

    @NonNull
    private final FinancialDataQueryService financialDataQueryService;

    public void validateCreateInstallmentDto(InstallmentDto installmentDto) {
        assertNotNull(installmentDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.INSTALLMENT));
        assertNotNull(installmentDto.getValue(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.INSTALLMENT, ProjectExceptionCodes.VALUE));
    }

    public void validatePayDateNotFuture(LocalDate date) {
        assertIsTrue(!date.isAfter(LocalDate.now()),
                createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.INSTALLMENT,
                        ProjectExceptionCodes.PAY_DATE));
    }

    public void validateUpdateInstallmentDto(InstallmentDto installmentDto, boolean paid) {
        assertNotNull(installmentDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.INSTALLMENT));
        assertNotNull(installmentDto.getValue(),
                createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.INSTALLMENT, ProjectExceptionCodes.VALUE));
        if (paid) {
            this.validatePayDateNotFuture(installmentDto.getPaymentDate());
        }
    }

    public void validateInstallmentExistence(Long installmentId) {
        assertIsTrue(this.financialDataQueryService.doesInstallmentExistsByInstallmentId(installmentId),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.INSTALLMENT),
                installmentId);
    }


    public void validatePayInstallmentDto(InstallmentDto installmentDto, boolean paid) {
        if (paid) {
            throw new IllegalArgumentException(
                    BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID, ProjectExceptionCodes.INSTALLMENT,
                            ProjectExceptionCodes.PAID));
        }
        assertNotNull(installmentDto, createMessageCode(ExceptionCodes.NULL, ProjectExceptionCodes.INSTALLMENT));
        this.validatePayDateNotFuture(installmentDto.getPaymentDate());
    }
}
