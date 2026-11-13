package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.common.model.RevInfo;
import com.arturjarosz.task.project.query.ProjectHistoryQueryService;
import com.arturjarosz.task.project.query.RecordRevision;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.RevisionType;
import org.hibernate.envers.query.AuditEntity;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Reads the audit tables written by Hibernate Envers. Which stages a project had and which tasks a stage had is kept
 * only in the audit join tables of their collections, which Envers queries can't read, so those are native queries;
 * {h-schema} is replaced by Hibernate with the default schema.
 */
@Finder
public class ProjectHistoryQueryServiceImpl implements ProjectHistoryQueryService {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public <T> List<RecordRevision<T>> getRevisions(Class<T> recordClass, Collection<Long> recordIds) {
        if (recordIds.isEmpty()) {
            return List.of();
        }
        List<?> rows = AuditReaderFactory.get(this.entityManager).createQuery()
                .forRevisionsOfEntity(recordClass, false, true)
                .add(AuditEntity.id().in(recordIds))
                .addOrder(AuditEntity.revisionNumber().asc())
                .getResultList();
        return rows.stream()
                .map(Object[].class::cast)
                .map(row -> toRecordRevision(recordClass, row))
                .toList();
    }

    @Override
    public Set<Long> getStageIdsOfProject(long projectId) {
        List<Long> stageIds = this.entityManager.createNativeQuery(
                        "SELECT DISTINCT STAGE_ID FROM {h-schema}PROJECT_STAGE_AUD WHERE PROJECT_ID = :projectId",
                        Long.class)
                .setParameter("projectId", projectId)
                .getResultList();
        return new HashSet<>(stageIds);
    }

    @Override
    public Map<Long, Long> getStageIdsByTaskId(Collection<Long> stageIds) {
        if (stageIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> rows = this.entityManager.createNativeQuery(
                        "SELECT DISTINCT TASK_ID, STAGE_ID FROM {h-schema}STAGE_TASK_AUD WHERE STAGE_ID IN (:stageIds)",
                        Object[].class)
                .setParameter("stageIds", stageIds)
                .getResultList();
        return rows.stream().collect(Collectors.toMap(row -> ((Number) row[0]).longValue(),
                row -> ((Number) row[1]).longValue()));
    }

    // Envers returns each revision as the record, the revision entity and the revision type
    private static <T> RecordRevision<T> toRecordRevision(Class<T> recordClass, Object[] row) {
        var revInfo = (RevInfo) row[1];
        return new RecordRevision<>(recordClass.cast(row[0]), revInfo.getRev(), revInfo.getRevtstmp(),
                revInfo.getAuthorAuthId(), (RevisionType) row[2]);
    }
}
