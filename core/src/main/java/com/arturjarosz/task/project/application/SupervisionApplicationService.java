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

    SupervisionDto updateSupervision();

    void deleteSupervision();

    /**
     * Creates SupervisionVisit on Supervision for Project with given projectId. If Project does not exist or id does not
     * have Supervision then new exception will be thrown.
     *
     * @param projectId
     * @param supervisionVisitDto
     * @return
     */
    SupervisionVisitDto createSupervisionVisit(Long projectId, SupervisionVisitDto supervisionVisitDto);
}
