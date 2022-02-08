package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.InstallmentDto;

import java.util.List;

public interface InstallmentApplicationService {

    /**
     * Creates installment according to installmentDto for stage in given project.
     * If project or stage do not exist, exception is thrown and installment is not created.
     */
    InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Updates installment according to installmentDto for stage in given project.
     * If project,stage or installment does not exist, exception is thrown and installment is not updated.
     */
    InstallmentDto updateInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Remove installment from stage on project of given Id.
     */
    void removeInstallment(Long projectId, Long stageId);

    /**
     * Mark Installment given installmentDto for Stage with given stageId on Project with given projectId.
     */
    InstallmentDto payInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Loads all installments for given Project with projectId.
     */
    List<InstallmentDto> getInstallmentList(Long projectId);

    /**
     * Loads Installments connected to Stage with given stageId on Project with given projectId.
     */
    InstallmentDto getInstallment(Long projectId, Long stageId);
}

