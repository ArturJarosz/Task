package com.arturjarosz.task.supervision.application;

import com.arturjarosz.task.supervision.application.dto.SupervisionDto;
import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;

public interface SupervisionApplicationService {

    /**
     * Creates Supervision with data passed with supervisionDto. If Project specified with projectId inside
     * supervisionDto does not exist, a new exception will be thrown.
     */
    SupervisionDto createSupervision(SupervisionDto supervisionDto);

    /**
     * Updates Supervision supervisionId according to data in supervisionDto. If Supervision does not exists, then new
     * exception will be thrown.
     */
    SupervisionDto updateSupervision(Long supervisionId, SupervisionDto supervisionDto);

    /**
     * Removes Supervision with supervisionId.
     */
    void deleteSupervision(Long supervisionId);

    /**
     * Loads supervisionDto of supervision with given supervisionId.
     */
    SupervisionDto getSupervision(Long supervisionId);

    /**
     * Creates SupervisionVisit on Supervision with supervisionId. If Supervision does not exist then new exception
     * will be thrown.
     */
    SupervisionVisitDto createSupervisionVisit(Long supervisionId, SupervisionVisitDto supervisionVisitDto);

    /**
     * Updates SupervisionVisit on Supervision with supervisionId. If Supervision does not exist then new exception
     * will be thrown.
     */
    SupervisionVisitDto updateSupervisionVisit(Long supervisionId, Long supervisionVisitId,
                                               SupervisionVisitDto supervisionVisitDto);

    /**
     * Loads supervisionVision of given supervisionVisitId for supervision with supervisionId.
     */
    SupervisionVisitDto getSupervisionVisit(Long supervisionId, Long supervisionVisitId);

    /**
     * Removes supervisionVisit of given supervisionVisitId for supervision with supervisionId.
     */
    void deleteSupervisionVisit(Long supervisionId, Long supervisionVisitId);

}
