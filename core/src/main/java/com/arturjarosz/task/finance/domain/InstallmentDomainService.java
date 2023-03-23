package com.arturjarosz.task.finance.domain;

import com.arturjarosz.task.finance.application.dto.InstallmentDto;
import com.arturjarosz.task.finance.model.Installment;

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
    Installment payInstallment(Installment installment, LocalDate payDate);
}
