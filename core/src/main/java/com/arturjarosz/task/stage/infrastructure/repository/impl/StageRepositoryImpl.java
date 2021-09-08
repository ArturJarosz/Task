package com.arturjarosz.task.stage.infrastructure.repository.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import com.arturjarosz.task.stage.infrastructure.repository.StageRepository;
import com.arturjarosz.task.stage.model.QStage;
import com.arturjarosz.task.stage.model.Stage;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class StageRepositoryImpl extends GenericJpaRepositoryImpl<Stage, QStage> implements StageRepository {

    public static final QStage STAGE = QStage.stage;

    public StageRepositoryImpl() {
        super(STAGE);
    }
}
