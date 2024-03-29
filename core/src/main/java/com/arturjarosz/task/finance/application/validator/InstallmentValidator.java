package com.arturjarosz.task.finance.application.validator;

import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.finance.model.Installment;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityNotNull;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertEntityPresent;
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
        this.validateValueNotNegative(installmentDto.getValue());
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
        this.validateValueNotNegative(installmentDto.getValue());
    }

    public void validateInstallmentExistence(Long installmentId) {
        assertEntityPresent(this.financialDataQueryService.doesInstallmentExistsByInstallmentId(installmentId),
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.INSTALLMENT),
                installmentId);
    }

    public void validateInstallmentExistence(Installment installment, long installmentId, long projectId) {
        assertEntityNotNull(installment,
                BaseValidator.createMessageCode(ExceptionCodes.NOT_EXIST, ProjectExceptionCodes.INSTALLMENT,
                        ProjectExceptionCodes.PROJECT), installmentId, projectId);
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

    private void validateValueNotNegative(BigDecimal value) {
        assertIsTrue(value.doubleValue() >= 0.0D,
                createMessageCode(ExceptionCodes.NEGATIVE, ProjectExceptionCodes.INSTALLMENT,
                        ProjectExceptionCodes.VALUE));
    }

    public void validateInstallmentNotPaid(Installment installment) {
        assertIsTrue(!installment.isPaid(),
                createMessageCode(ProjectExceptionCodes.ALREADY_PAID, ProjectExceptionCodes.INSTALLMENT),
                installment.getId());
    }
}
