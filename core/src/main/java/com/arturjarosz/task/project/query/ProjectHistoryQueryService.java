package com.arturjarosz.task.project.query;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ProjectHistoryQueryService {

    /**
     * Returns the revisions of the records of the given class with the given ids, oldest first.
     */
    <T> List<RecordRevision<T>> getRevisions(Class<T> recordClass, Collection<Long> recordIds);

    /**
     * Returns the ids of every stage the project has had since its history began, removed ones too.
     */
    Set<Long> getStageIdsOfProject(long projectId);

    /**
     * Returns the stage of every task the given stages have had since their history began, removed tasks too.
     */
    Map<Long, Long> getStageIdsByTaskId(Collection<Long> stageIds);
}
