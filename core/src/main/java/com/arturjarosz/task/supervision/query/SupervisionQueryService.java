package com.arturjarosz.task.supervision.query;

import com.arturjarosz.task.dto.SupervisionVisitDto;
import com.arturjarosz.task.supervision.model.Supervision;

import java.util.List;

public interface SupervisionQueryService {

    boolean supervisionExists(Long supervisionId);

    boolean supervisionVisitExistsInSupervision(Long supervisionId, Long supervisionVisitId);

    SupervisionVisitDto getSupervisionVisit(Long supervisionVisitId);

    List<SupervisionVisitDto> getSupervisionVisits(Long supervisionId);

    long getProjectIdForSupervision(long supervisionId);

    boolean supervisionOnProjectExistence(Long projectId);

    Supervision getSupervisionByProjectId(long projectId);
}
