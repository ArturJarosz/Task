package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.QCost;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.model.QStage;
import com.arturjarosz.task.project.model.Stage;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private static final QProject PROJECT = QProject.project;

    private static final QCost COST = QCost.cost;
    private static final QStage STAGE = QStage.stage;

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

}
