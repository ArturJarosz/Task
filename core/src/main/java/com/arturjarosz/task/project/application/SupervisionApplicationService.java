package com.arturjarosz.task.project.application;

import com.arturjarosz.task.project.application.dto.SupervisionDto;
import com.arturjarosz.task.project.application.dto.SupervisionVisitDto;

public interface SupervisionApplicationService {

    /**
     * Creates Supervision for Project with given projectId. If Project does not exist, a new exception will be thrown.
     *
     * @param projectId
     * @param supervisionDto
     * @return
     */
    SupervisionDto createSupervision(Long projectId, SupervisionDto supervisionDto);

    /**
     * Updates Supervision for Project with projectId according to data in supervisionDto. If Project does not have
     * a Supervision then new exception will be thrown.
     * @param projectId
     * @param supervisionDto
     * @return
     */
    SupervisionDto updateSupervision(Long projectId, SupervisionDto supervisionDto);

    void deleteSupervision(Long projectId);

    SupervisionDto getSupervision(Long projectId);

    /**
     * Creates SupervisionVisit on Supervision for Project with given projectId. If Project does not exist or id does not
     * have Supervision then new exception will be thrown.
     *
     * @param projectId
     * @param supervisionVisitDto
     * @return
     */
    SupervisionVisitDto createSupervisionVisit(Long projectId, SupervisionVisitDto supervisionVisitDto);

    /**
     * Get
     *
     * @param projectId
     * @param supervisionVisitId
     * @return
     */
    SupervisionVisitDto getSupervisionVisit(Long projectId, Long supervisionVisitId);
}
