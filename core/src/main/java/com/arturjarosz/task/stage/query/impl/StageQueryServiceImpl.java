package com.arturjarosz.task.stage.query.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.stage.model.QStage;
import com.arturjarosz.task.stage.model.Stage;
import com.arturjarosz.task.stage.query.StageQueryService;
import org.springframework.stereotype.Repository;

@Repository
public class StageQueryServiceImpl extends AbstractQueryService<QStage> implements StageQueryService {

    private static final QStage STAGE = QStage.stage;

    public StageQueryServiceImpl() {
        super(STAGE);
    }

    @Override
    public Stage getStageById(Long stageId) {
        return this.query().from(STAGE).select(STAGE).where(STAGE.id.eq(stageId)).fetchOne();
    }
}
