package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.CooperatorJobType;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QCooperatorJob;
import com.arturjarosz.task.project.model.QCost;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.QSupply;
import com.arturjarosz.task.project.model.QTask;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.types.Projections;

import java.util.List;

@Finder
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;

    private static final QCooperatorJob COOPERATOR_JOB = QCooperatorJob.cooperatorJob;
    private static final QCost COST = QCost.cost;
    private static final QStage STAGE = QStage.stage;
    private static final QTask TASK = QTask.task;
    private static final QSupply SUPPLY = QSupply.supply;

    public ProjectQueryServiceImpl() {
        super(PROJECT);
    }

    @Override
    public Cost getCostById(Long costId) {
        return this.query().from(COST).select(COST).where(COST.id.eq(costId)).fetchOne();
    }

    @Override
    public Stage getStageById(Long stageId) {
        return this.query().from(STAGE).select(STAGE).where(STAGE.id.eq(stageId)).fetchOne();
    }

    @Override
    public CooperatorJob getCooperatorJobByIdForProject(Long cooperatorJobId) {
        return this.query().from(COOPERATOR_JOB).select(COOPERATOR_JOB).where(COOPERATOR_JOB.id.eq(cooperatorJobId))
                .fetchOne();
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
        return this.query().from(TASK).where(TASK.id.eq(taskId))
                .select(Projections.bean(TaskDto.class,
                        TASK.id.as(TaskDto.ID),
                        TASK.status.as(TaskDto.STATUS),
                        TASK.name.as(TaskDto.NAME),
                        TASK.startDate.as(TaskDto.START_DATE),
                        TASK.endDate.as(TaskDto.END_DATE),
                        TASK.note.as(TaskDto.NOTE),
                        TASK.type.as(TaskDto.TASK_TYPE)
                ))
                .fetchOne();
    }

    @Override
    public List<StageDto> getStagesForProjectById(Long projectId) {
        return this.query().from(PROJECT).leftJoin(PROJECT.stages, STAGE).where(PROJECT.id.eq(projectId))
                .select(Projections.bean(StageDto.class,
                        STAGE.id.as(StageDto.ID),
                        STAGE.name.as(StageDto.NAME),
                        STAGE.deadline.as(StageDto.DEADLINE),
                        STAGE.stageType.as(StageDto.STAGE_TYPE),
                        STAGE.status.as(StageDto.STATUS)))
                .fetch();
    }

    //TODO TA-191
    @Override
    public CooperatorJob getCooperatorJobOfTypeExistsOnProject(Long projectId, Long cooperatorJobId,
                                                               CooperatorJobType cooperatorJobType) {
/*        return this.query().from(PROJECT)
                .where(PROJECT.id.eq(projectId))
                .join(PROJECT.cooperatorJobs, COOPERATOR_JOB)
                .where(COOPERATOR_JOB.id.eq(cooperatorJobId).and(COOPERATOR_JOB.type.eq(cooperatorJobType)))
                .select(COOPERATOR_JOB)
                .fetchOne();*/
        return null;
    }

    @Override
    public SupplyDto getSupplyForProject(long supplyId, long projectId) {
        return this.query()
                .from(COOPERATOR_JOB)
                .where(COOPERATOR_JOB.id.eq(supplyId).and(COOPERATOR_JOB.type.eq(CooperatorJobType.SUPPLY)))
                .select(Projections.bean(SupplyDto.class))
                .fetchOne();
    }

    @Override
    public ContractorJobDto getContractorJobForProject(long contractorJobId,
                                                       long projectId) {
        return this.query()
                .from(COOPERATOR_JOB)
                .where(COOPERATOR_JOB.id.eq(contractorJobId).and(COOPERATOR_JOB.type.eq(CooperatorJobType.SUPPLY)))
                .select(Projections.bean(ContractorJobDto.class))
                .fetchOne();
    }

}
