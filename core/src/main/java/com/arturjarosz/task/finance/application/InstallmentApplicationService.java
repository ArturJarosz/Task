package com.arturjarosz.task.finance.application;

import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.InstallmentProjectDataDto;

public interface InstallmentApplicationService {

    /**
     * Creates Installment according to installmentDto for stage in given project.
     * If project or stage do not exist, exception is thrown and installment is not created.
     */
    InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Creates the deposit of the project according to installmentDto, as part of signing its contract. A deposit
     * belongs to the project, not to a stage. If the project does not exist, its contract has not been signed or it
     * already has a deposit, exception is thrown and the deposit is not created.
     */
    InstallmentDto createDeposit(Long projectId, InstallmentDto installmentDto);

    /**
     * Checks that the contract of the project can be signed with the given deposit: a valid deposit when the project
     * has none, no deposit when it already has one.
     */
    void validateDepositForSigning(Long projectId, InstallmentDto deposit);

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
     * Loads data related to project installments for given projectId.
     */
    InstallmentProjectDataDto getProjectInstallmentsData(Long projectId);

    /**
     * Loads Installments with given installmentId on Project with given projectId.
     */
    InstallmentDto getInstallment(Long projectId, Long installmentId);
}

