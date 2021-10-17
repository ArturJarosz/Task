package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.InstallmentValidator;
import com.arturjarosz.task.project.application.ProjectExceptionCodes;
import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.project.domain.InstallmentDomainService;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

import java.time.LocalDate;

@DomainService
public class InstallmentDomainServiceImpl implements InstallmentDomainService {

    public InstallmentDomainServiceImpl() {
        //needed by Hibernate
    }

    @Override
    public Installment updateInstallment(Installment installment, InstallmentDto installmentDto) {
        if (installment.isPaid()) {
            InstallmentValidator.validatePayDateNotFuture(installmentDto.getPaymentDate());
        }
        installment.update(installmentDto);
        return installment;
    }

    @Override
    public Installment payInstallment(Stage stage, LocalDate payDate) {
        if (payDate != null) {
            InstallmentValidator.validatePayDateNotFuture(payDate);
        } else {
            payDate = LocalDate.now();
        }
        Installment installment = stage.getInstallment();
        if (installment.isPaid()) {
            throw new IllegalStateException(BaseValidator.createMessageCode(ExceptionCodes.NOT_VALID,
                    ProjectExceptionCodes.INSTALLMENT, ProjectExceptionCodes.PAID));
        }
        installment.payInstallment(payDate);
        return installment;
    }
}
