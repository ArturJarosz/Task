package com.arturjarosz.task.project.domain.impl;

import com.arturjarosz.task.project.application.InstallmentValidator;
import com.arturjarosz.task.project.domain.InstallmentDomainService;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.sharedkernel.annotations.DomainService;

import java.time.LocalDate;

@DomainService
public class InstallmentDomainServiceImpl implements InstallmentDomainService {

    public InstallmentDomainServiceImpl() {
    }

    @Override
    public void updateInstallment(Stage stage, Double value, LocalDate payDate, String description) {
        Installment installment = stage.getInstallment();
        if (installment.isPaid()) {
            installment.update(value, description, null);
        } else {
            InstallmentValidator.validatePayDateNotFuture(payDate);
            installment.update(value, description, payDate);
        }
    }

    @Override
    public void payForInstallment(Stage stage, LocalDate payDate) {
        if (payDate != null) {
            InstallmentValidator.validatePayDateNotFuture(payDate);
        } else {
            payDate = LocalDate.now();
        }
        Installment installment = stage.getInstallment();
        installment.payInstallment(payDate);
    }
}
