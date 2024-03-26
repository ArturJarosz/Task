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
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Objects;

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
