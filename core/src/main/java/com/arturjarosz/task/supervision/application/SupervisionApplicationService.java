package com.arturjarosz.task.supervision.application;

import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;

public interface SupervisionApplicationService {

    /**
     * Creates Supervision with data passed with supervisionDto. If Project specified with projectId inside
     * supervisionDto does not exist, a new exception will be thrown.
     *
     * @param supervisionDto
     * @return
     */
    SupervisionDto createSupervision(SupervisionDto supervisionDto);

    /**
     * Updates Supervision supervisionId according to data in supervisionDto. If Supervision does not exists, then new
     * exception will be thrown.
     *
     * @param supervisionId
     * @param supervisionDto
     * @return
     */
    SupervisionDto updateSupervision(Long supervisionId, SupervisionDto supervisionDto);

    /**
     * Removes Supervision with supervisionId.
     *
     * @param supervisionId
     */
    void deleteSupervision(Long supervisionId);

    SupervisionDto getSupervision(Long supervisionId);

    /**
     * Creates SupervisionVisit on Supervision with supervisionId. If Supervision does not exist then new exception
     * will be thrown.
     *
     * @param supervisionId
     * @param supervisionVisitDto
     * @return
     */
    SupervisionVisitDto createSupervisionVisit(Long supervisionId, SupervisionVisitDto supervisionVisitDto);

    /**
     * Updates SupervisionVisit on Supervision with supervisionId. If Supervision does not exist then new exception
     * will be thrown.
     *
     * @param supervisionId
     * @param supervisionVisitDto
     * @return
     */
    SupervisionVisitDto updateSupervisionVisit(Long supervisionId, Long supervisionVisitId,
                                               SupervisionVisitDto supervisionVisitDto);

    /**
     * Get
     *
     * @param supervisionId
     * @param supervisionVisitId
     * @return
     */
    SupervisionVisitDto getSupervisionVisit(Long supervisionId, Long supervisionVisitId);

    void deleteSupervisionVisit(Long supervisionId, Long supervisionVisitId);

}
