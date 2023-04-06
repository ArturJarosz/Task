package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.domain.dto.ProjectStatusData;
import com.arturjarosz.task.project.domain.dto.StageStatusData;
import com.arturjarosz.task.project.model.*;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.types.Projections;

import java.util.List;

@Finder
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;
    private static final QContract CONTRACT = QContract.contract;
    private static final QStage STAGE = QStage.stage;
    private static final QTask TASK = QTask.task;

    public ProjectQueryServiceImpl() {
        super(PROJECT);
    }

    @Override
    public Stage getStageById(Long stageId) {
        return this.query().from(STAGE).select(STAGE).where(STAGE.id.eq(stageId)).fetchOne();
    }

    @Override
    public List<Project> getProjectsForClientId(Long clientId) {
        return this.query().from(PROJECT).select(PROJECT).where(PROJECT.clientId.eq(clientId)).fetch();
    }

    @Override
    public List<Project> getProjectsForArchitect(Long architectId) {
        return this.query().from(PROJECT).select(PROJECT).where(PROJECT.architectId.eq(architectId)).fetch();
    }

    @Override
    public TaskDto getTaskByTaskId(Long taskId) {
        return this.query()
                .from(TASK)
                .where(TASK.id.eq(taskId))
                .select(Projections.bean(TaskDto.class, TASK.id.as(TaskDto.ID_FIELD),
                        TASK.status.as(TaskDto.STATUS_FIELD),
                        TASK.name.as(TaskDto.NAME_FIELD), TASK.startDate.as(TaskDto.START_DATE_FIELD),
                        TASK.endDate.as(TaskDto.END_DATE_FIELD), TASK.note.as(TaskDto.NOTE_FIELD),
                        TASK.type.as(TaskDto.TASK_TYPE_FIELD)))
                .fetchOne();
    }

    @Override
    public List<StageDto> getStagesForProjectById(Long projectId) {
        return this.query()
                .from(PROJECT)
                .leftJoin(PROJECT.stages, STAGE)
                .where(PROJECT.id.eq(projectId))
                .select(Projections.bean(StageDto.class, STAGE.id.as(StageDto.ID_FIELD),
                        STAGE.name.as(StageDto.NAME_FIELD),
                        STAGE.deadline.as(StageDto.DEADLINE_FIELD), STAGE.stageType.as(StageDto.STAGE_TYPE_FIELD),
                        STAGE.status.as(StageDto.STATUS_FIELD)))
                .fetch();
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
    public ProjectStatusData getProjectStatusData(long projectId) {
        return this.query()
                .from(PROJECT)
                .where(PROJECT.id.eq(projectId))
                .select(Projections.constructor(ProjectStatusData.class, PROJECT.status, PROJECT.workflowName))
                .fetchOne();
    }

    @Override
    public StageStatusData getStageStatusData(long stageId) {
        return this.query()
                .from(STAGE)
                .where(STAGE.id.eq(stageId))
                .select(Projections.constructor(StageStatusData.class, STAGE.status, STAGE.workflowName))
                .fetchOne();

    }

    @Override
    public Boolean doesProjectExistByProjectId(Long projectId) {
        return this.queryFromAggregate().where(PROJECT.id.eq(projectId)).fetchCount() > 0;
    }
}
