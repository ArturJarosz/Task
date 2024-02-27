package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.dto.StageDto;
import com.arturjarosz.task.dto.TaskDto;
import com.arturjarosz.task.project.application.mapper.StageDtoMapper;
import com.arturjarosz.task.project.application.mapper.TaskDtoMapper;
import com.arturjarosz.task.project.domain.dto.ProjectStatusData;
import com.arturjarosz.task.project.domain.dto.StageStatusData;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.QTask;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.types.Projections;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Objects;

@Finder
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;
    private static final QContract CONTRACT = QContract.contract;
    private static final QStage STAGE = QStage.stage;
    private static final QTask TASK = QTask.task;
    private static final TaskDtoMapper TASK_DTO_MAPPER = Mappers.getMapper(TaskDtoMapper.class);
    private static final StageDtoMapper STAGE_DTO_MAPPER = Mappers.getMapper(StageDtoMapper.class);

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
        var task = this.query().from(TASK).where(TASK.id.eq(taskId)).select(TASK).fetchOne();
        return TASK_DTO_MAPPER.taskToTaskDto(task);
    }

    @Override
    public List<StageDto> getStagesForProjectById(Long projectId) {
        List<Stage> stages = this.query()
                .from(PROJECT)
                .leftJoin(PROJECT.stages, STAGE)
                .where(PROJECT.id.eq(projectId))
                .select(STAGE)
                .fetch();
        return stages.stream().filter(Objects::nonNull).map(STAGE_DTO_MAPPER::stageDtoFromStage).toList();
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
