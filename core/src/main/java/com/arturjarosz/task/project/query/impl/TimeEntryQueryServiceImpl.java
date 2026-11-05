package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.QTask;
import com.arturjarosz.task.project.model.QTimeEntry;
import com.arturjarosz.task.project.model.TimeEntry;
import com.arturjarosz.task.project.query.TimeEntryQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.jpa.JPQLQuery;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Finder
public class TimeEntryQueryServiceImpl extends AbstractQueryService<QTimeEntry> implements TimeEntryQueryService {

    private static final QProject PROJECT = QProject.project;
    private static final QStage STAGE = QStage.stage;
    private static final QTask TASK = QTask.task;
    private static final QTimeEntry TIME_ENTRY = QTimeEntry.timeEntry;

    public TimeEntryQueryServiceImpl() {
        super(TIME_ENTRY);
    }

    @Override
    public List<TimeEntry> getTimeEntriesForTask(long taskId) {
        return this.query()
                .selectFrom(TIME_ENTRY)
                .where(TIME_ENTRY.taskId.eq(taskId))
                .orderBy(TIME_ENTRY.workDate.desc(), TIME_ENTRY.createdDateTime.desc(), TIME_ENTRY.id.desc())
                .fetch();
    }

    @Override
    public boolean timeEntryExistsInTask(long taskId, long timeEntryId) {
        return this.query()
                .selectOne()
                .from(TIME_ENTRY)
                .where(TIME_ENTRY.id.eq(timeEntryId).and(TIME_ENTRY.taskId.eq(taskId)))
                .fetchFirst() != null;
    }

    @Override
    public boolean architectHasTimeEntries(long architectId) {
        return this.query()
                .selectOne()
                .from(TIME_ENTRY)
                .where(TIME_ENTRY.architectId.eq(architectId))
                .fetchFirst() != null;
    }

    @Override
    public Map<Long, Long> getMinutesByTaskIds(Collection<Long> taskIds) {
        return this.getMinutesBy(TIME_ENTRY.taskId, taskIds, this.query().from(TIME_ENTRY));
    }

    @Override
    public Map<Long, Long> getMinutesByStageIds(Collection<Long> stageIds) {
        return this.getMinutesBy(STAGE.id, stageIds, this.query()
                .from(STAGE)
                .join(STAGE.tasks, TASK)
                .join(TIME_ENTRY)
                .on(TIME_ENTRY.taskId.eq(TASK.id)));
    }

    @Override
    public long getMinutesForProject(long projectId) {
        Tuple row = this.query()
                .from(PROJECT)
                .join(PROJECT.stages, STAGE)
                .join(STAGE.tasks, TASK)
                .join(TIME_ENTRY)
                .on(TIME_ENTRY.taskId.eq(TASK.id))
                .where(PROJECT.id.eq(projectId))
                .groupBy(PROJECT.id)
                .select(PROJECT.id, TIME_ENTRY.durationMinutes.sum())
                .fetchOne();
        return row == null ? 0L : toLong(row);
    }

    private Map<Long, Long> getMinutesBy(NumberPath<Long> ownerId, Collection<Long> ownerIds, JPQLQuery<?> entries) {
        Map<Long, Long> minutesByOwnerId = new HashMap<>();
        ownerIds.stream().filter(Objects::nonNull).forEach(id -> minutesByOwnerId.put(id, 0L));
        if (minutesByOwnerId.isEmpty()) {
            return minutesByOwnerId;
        }
        entries.where(ownerId.in(minutesByOwnerId.keySet()))
                .groupBy(ownerId)
                .select(ownerId, TIME_ENTRY.durationMinutes.sum())
                .fetch()
                .forEach(row -> minutesByOwnerId.put(row.get(ownerId), toLong(row)));
        return minutesByOwnerId;
    }

    // JPQL sums integers as Long, while QueryDSL types the sum as Integer, so it is read as a Number
    private static long toLong(Tuple row) {
        Number sum = row.get(1, Number.class);
        return sum == null ? 0L : sum.longValue();
    }
}
