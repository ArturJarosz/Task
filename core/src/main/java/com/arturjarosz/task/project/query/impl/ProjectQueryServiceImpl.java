package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.project.application.dto.TaskDto;
import com.arturjarosz.task.project.model.CooperatorJob;
import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.Project;
import com.arturjarosz.task.project.model.QCooperatorJob;
import com.arturjarosz.task.project.model.QCost;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.QTask;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.querydsl.core.types.Projections;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;

    private static final QCooperatorJob COOPERATOR_JOB = QCooperatorJob.cooperatorJob;
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
    public TaskDto getTaskForProjectAndStage(Long taskId) {
        return this.query().from(TASK).where(TASK.id.eq(taskId))
                .select(Projections.bean(TaskDto.class, TASK.status.as(TaskDto.STATUS_NAME)))
                .fetchOne();
    }

}
