package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.application.dto.InstallmentDto;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Stage;

import java.time.LocalDate;

public interface InstallmentDomainService {
    /**
     * Update information on installment.
     *
     */
    Installment updateInstallment(Installment installment, InstallmentDto installmentDto);

    /**
     * Mark installment as paid and puts passed date as pay date. If payDate is null, then today is set as a pay date.
     */
    Installment payInstallment(Stage stage, LocalDate payDate);
}
