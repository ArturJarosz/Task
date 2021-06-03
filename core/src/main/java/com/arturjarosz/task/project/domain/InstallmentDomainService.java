package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.project.model.Stage;

import java.time.LocalDate;

public interface InstallmentDomainService {
    /**
     * Update information on installment.
     *
     * @param stage
     * @param value
     * @param payDate
     * @param note
     */
    Installment updateInstallment(Stage stage, Double value, LocalDate payDate, String note);

    /**
     * Mark installment as paid and puts passed date as pay date. If payDate is null, then today is set as a pay date.
     *
     * @param stage
     * @param payDate
     */
    Installment payForInstallment(Stage stage, LocalDate payDate);
}
