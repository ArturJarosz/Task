package com.arturjarosz.task.supervision.query;

import com.arturjarosz.task.supervision.application.dto.SupervisionVisitDto;

public interface SupervisionQueryService {

    boolean supervisionExists(Long supervisionId);

    boolean supervisionVisitExistsInSupervision(Long supervisionId, Long supervisionVisitId);

    SupervisionVisitDto getSupervisionVisit(Long supervisionVisitId);

    long getProjectIdForSupervision(Long supervisionId);

    boolean supervisionOnProjectExistence(Long projectId);
}
