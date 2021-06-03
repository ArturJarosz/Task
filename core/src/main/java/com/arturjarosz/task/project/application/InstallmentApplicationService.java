package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.InstallmentDto;

import java.util.List;

public interface InstallmentApplicationService {

    /**
     * Creates installment according to installmentDto for stage in given project.
     * If project or stage do not exist, exception is thrown and installment is not created.
     *
     * @param projectId
     * @param stageId
     * @param installmentDto
     * @return
     */
    InstallmentDto createInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Updates installment according to installmentDto for stage in given project.
     * If project,stage or installment does not exist, exception is thrown and installment is not updated.
     *
     * @param projectId
     * @param stageId
     * @param installmentDto
     * @return
     */
    InstallmentDto updateInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    /**
     * Remove installment from stage on project of given Id.
     *
     * @param projectId
     * @param stageId
     */
    void removeInstallment(Long projectId, Long stageId);

    /**
     * @param projectId
     * @param stageId
     * @param installmentDto
     * @return
     */
    InstallmentDto payInstallment(Long projectId, Long stageId, InstallmentDto installmentDto);

    List<InstallmentDto> getInstallmentList(Long projectId);

    InstallmentDto getInstallment(Long projectId, Long stageId);
}

