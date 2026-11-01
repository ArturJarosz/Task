package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.finance.model.QInstallment;
import com.arturjarosz.task.project.application.mapper.StageMapper;
import com.arturjarosz.task.project.application.mapper.TaskMapper;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.QTask;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectCounts;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.NumberExpression;
import com.querydsl.core.types.dsl.NumberPath;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Finder
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;
    private static final QContract CONTRACT = QContract.contract;
    private static final QStage STAGE = QStage.stage;
    private static final QTask TASK = QTask.task;
    private static final QInstallment INSTALLMENT = QInstallment.installment;
    private final TaskMapper taskMapper;
    private final StageMapper stageMapper;

    @Autowired
    public ProjectQueryServiceImpl(StageMapper stageMapper, TaskMapper taskMapper) {
        super(PROJECT);
        this.stageMapper = stageMapper;
        this.taskMapper = taskMapper;
    }

    @Override
    public Stage getStageById(long stageId) {
        return this.query().from(STAGE).select(STAGE).where(STAGE.id.eq(stageId)).fetchOne();
    }

    @Override
    public List<Project> getProjectsForClientId(long clientId) {
        return this.query().from(PROJECT).select(PROJECT).where(PROJECT.clientId.eq(clientId)).fetch();
    }

    @Override
    public List<Project> getProjectsForArchitect(long architectId) {
        return this.query().from(PROJECT).select(PROJECT).where(PROJECT.architectId.eq(architectId)).fetch();
    }

    @Override
    public Map<Long, ProjectCounts> getProjectCountsByArchitectIds(Collection<Long> architectIds,
            Set<ProjectStatus> activeStatuses) {
        return this.getProjectCountsBy(PROJECT.architectId, architectIds, activeStatuses);
    }

    @Override
    public Map<Long, ProjectCounts> getProjectCountsByClientIds(Collection<Long> clientIds,
            Set<ProjectStatus> activeStatuses) {
        return this.getProjectCountsBy(PROJECT.clientId, clientIds, activeStatuses);
    }

    @Override
    public Map<Long, ProjectCounts> getProjectCountsByProjectTypeIds(Collection<Long> projectTypeIds) {
        // project types have no active projects of their own, only the number of all projects is used
        return this.getProjectCountsBy(PROJECT.projectTypeId, projectTypeIds, Set.of());
    }

    private Map<Long, ProjectCounts> getProjectCountsBy(NumberPath<Long> ownerId, Collection<Long> ownerIds,
            Set<ProjectStatus> activeStatuses) {
        Map<Long, ProjectCounts> countsByOwnerId = new HashMap<>();
        ownerIds.stream().filter(Objects::nonNull).forEach(id -> countsByOwnerId.put(id, ProjectCounts.NONE));
        if (countsByOwnerId.isEmpty()) {
            return countsByOwnerId;
        }

        // An empty IN list is not valid JPQL, so with no active statuses no project counts as active
        NumberExpression<Long> activeCount = activeStatuses.isEmpty()
                ? Expressions.asNumber(0L)
                : new CaseBuilder().when(PROJECT.status.in(activeStatuses)).then(1L).otherwise(0L).sum();
        this.query()
                .from(PROJECT)
                .select(ownerId, PROJECT.count(), activeCount)
                .where(ownerId.in(countsByOwnerId.keySet()))
                .groupBy(ownerId)
                .fetch()
                .forEach(row -> countsByOwnerId.put(row.get(ownerId),
                        new ProjectCounts(Objects.requireNonNullElse(row.get(1, Long.class), 0L),
                                Objects.requireNonNullElse(row.get(2, Long.class), 0L))));
        return countsByOwnerId;
    }

    @Override
    public TaskDto getTaskByTaskId(long taskId) {
        var task = this.query().from(TASK).where(TASK.id.eq(taskId)).select(TASK).fetchOne();
        return this.taskMapper.taskToTaskDto(task);
    }

    @Override
    public List<StageDto> getStagesForProjectById(long projectId) {
        List<Stage> stages = this.query()
                .from(PROJECT)
                .leftJoin(PROJECT.stages, STAGE)
                .where(PROJECT.id.eq(projectId))
                .select(STAGE)
                .fetch();
        return stages.stream().filter(Objects::nonNull).map(this.stageMapper::mapToDto).toList();
    }

    @Override
    public ContractStatus getContractStatusForProject(long projectId) {
        return this.query()
                .from(PROJECT)
                .join(CONTRACT)
                .on(PROJECT.contractId.eq(CONTRACT.id))
                .where(PROJECT.id.eq(projectId))
                .select(CONTRACT.status)
                .fetchOne();
    }

    @Override
    public Boolean doesProjectExistByProjectId(long projectId) {
        return this.queryFromAggregate().where(PROJECT.id.eq(projectId)).fetchCount() > 0;
    }

    @Override
    public Long getInstallmentIdForStage(long stageId) {
        return this.query().from(INSTALLMENT).where(INSTALLMENT.stageId.eq(stageId)).select(INSTALLMENT.id).fetchOne();
    }
}
