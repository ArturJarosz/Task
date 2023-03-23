package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.finance.application.dto.InstallmentDto;

import java.util.List;

public interface InstallmentApplicationService {

    /**
     * Creates Installment according to installmentDto for stage in given project.
     * If project or stage do not exist, exception is thrown and installment is not created.
     */
    InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Updates Installment according to installmentDto in given project.
     * If project or installment does not exist, exception is thrown and installment is not updated.
     */
    InstallmentDto updateInstallment(Long projectId, Long installmentId, InstallmentDto installmentDto);

    /**
     * Remove Installment from the project with given installmentId.
     */
    void removeInstallment(Long projectId, Long installmentId);

    /**
     * Mark Installment with given installmentId on Project with given projectId.
     */
    InstallmentDto payInstallment(Long projectId, Long installmentId, InstallmentDto installmentDto);

    /**
     * Loads all installments for given Project with projectId.
     */
    List<InstallmentDto> getProjectInstallments(Long projectId);

    /**
     * Loads Installments with given installmentId on Project with given projectId.
     */
    InstallmentDto getInstallment(Long projectId, Long installmentId);
}

