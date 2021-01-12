package com.arturjarosz.task.project.query.impl;

import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.QCost;
import com.arturjarosz.task.project.model.QProject;
import com.arturjarosz.task.project.query.ProjectQueryService;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectQueryServiceImpl extends AbstractQueryService<QProject> implements ProjectQueryService {

    private final static QProject PROJECT = QProject.project;
    private final static QCost COST = QCost.cost;

    public ProjectQueryServiceImpl() {
        super(PROJECT);
    }

    @Override
    public Cost getCostById(Long costId) {
        return this.query().from(COST).select(COST).where(COST.id.eq(costId)).fetchOne();
    }

}
