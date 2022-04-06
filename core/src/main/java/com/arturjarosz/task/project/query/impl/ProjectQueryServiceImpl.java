package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.contract.status.ContractStatus;
import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.application.dto.StageDto;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.domain.dto.ProjectStatusData;
import com.arturjarosz.task.project.domain.dto.StageStatusData;
import com.arturjarosz.task.project.model.CooperatorJobType;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QContractorJob;
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
import com.querydsl.core.types.dsl.Expressions;

import java.util.List;

@Finder
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;

    private static final QContract CONTRACT = QContract.contract;
    private static final QContractorJob CONTRACTOR_JOB = QContractorJob.contractorJob;
    private static final QSupply SUPPLY = QSupply.supply;
    private static final QCost COST = QCost.cost;
    private static final QStage STAGE = QStage.stage;
    private static final QTask TASK = QTask.task;

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
                .select(Projections.bean(TaskDto.class, TASK.id.as(TaskDto.ID), TASK.status.as(TaskDto.STATUS),
                        TASK.name.as(TaskDto.NAME), TASK.startDate.as(TaskDto.START_DATE),
                        TASK.endDate.as(TaskDto.END_DATE), TASK.note.as(TaskDto.NOTE), TASK.type.as(TaskDto.TASK_TYPE)))
                .fetchOne();
    }

    @Override
    public List<StageDto> getStagesForProjectById(Long projectId) {
        return this.query().from(PROJECT).leftJoin(PROJECT.stages, STAGE).where(PROJECT.id.eq(projectId))
                .select(Projections.bean(StageDto.class, STAGE.id.as(StageDto.ID), STAGE.name.as(StageDto.NAME),
                        STAGE.deadline.as(StageDto.DEADLINE), STAGE.stageType.as(StageDto.STAGE_TYPE),
                        STAGE.status.as(StageDto.STATUS))).fetch();
    }

    @Override
    public SupplyDto getSupplyForProject(long supplyId, long projectId) {
        return this.query().from(SUPPLY).where(SUPPLY.id.eq(supplyId).and(SUPPLY.type.eq(CooperatorJobType.SUPPLY)))
                .select(Projections.bean(SupplyDto.class, SUPPLY.id, SUPPLY.name, SUPPLY.financialData.value.value,
                        SUPPLY.cooperatorId.as("supplierId"), SUPPLY.note, SUPPLY.financialData.hasInvoice,
                        SUPPLY.financialData.payable, SUPPLY.financialData.paid,
                        Expressions.asNumber(projectId).as("projectId"))).fetchOne();
    }

    @Override
    public ContractorJobDto getContractorJobForProject(long contractorJobId, long projectId) {
        return this.query().from(CONTRACTOR_JOB).where(CONTRACTOR_JOB.id.eq(contractorJobId)
                        .and(CONTRACTOR_JOB.type.eq(CooperatorJobType.CONTRACTOR_JOB)))
                .select(Projections.bean(ContractorJobDto.class, CONTRACTOR_JOB.id, CONTRACTOR_JOB.name,
                        CONTRACTOR_JOB.financialData.value.value, CONTRACTOR_JOB.cooperatorId.as("contractorId"),
                        CONTRACTOR_JOB.note, CONTRACTOR_JOB.financialData.hasInvoice,
                        CONTRACTOR_JOB.financialData.payable, CONTRACTOR_JOB.financialData.paid,
                        Expressions.asNumber(projectId).as("projectId"))).fetchOne();
    }

    @Override
    public ContractStatus getContractStatusForProject(long projectId) {
        return this.query().from(PROJECT).join(CONTRACT).on(PROJECT.contractId.eq(CONTRACT.id))
                .where(PROJECT.id.eq(projectId)).select(CONTRACT.status).fetchOne();

    }

    @Override
    public ProjectStatusData getProjectStatusData(long projectId) {
        return this.query().from(PROJECT).where(PROJECT.id.eq(projectId))
                .select(Projections.constructor(ProjectStatusData.class, PROJECT.status, PROJECT.workflowName))
                .fetchOne();
    }

    @Override
    public StageStatusData getStageStatusData(long stageId) {
        return this.query().from(STAGE).where(STAGE.id.eq(stageId))
                .select(Projections.constructor(StageStatusData.class, STAGE.status, STAGE.workflowName)).fetchOne();

    }
}
