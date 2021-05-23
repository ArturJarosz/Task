package com.arturjarosz.task.project.domain;

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
    public void updateInstallment(Stage stage, Double value, LocalDate payDate, String note);

    /**
     * Mark installment as paid and puts passed date as pay date. If payDate is null, then today is set as a pay date.
     *
     * @param stage
     * @param payDate
     */
    void payForInstallment(Stage stage, LocalDate payDate);
}
