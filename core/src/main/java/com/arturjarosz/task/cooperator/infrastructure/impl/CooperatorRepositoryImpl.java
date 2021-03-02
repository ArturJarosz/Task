package com.arturjarosz.task.cooperator.infrastructure.impl;

import com.arturjarosz.task.cooperator.infrastructure.CooperatorRepository;
import com.arturjarosz.task.cooperator.model.Cooperator;
import com.arturjarosz.task.cooperator.model.QCooperator;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CooperatorRepositoryImpl extends GenericJpaRepositoryImpl<Cooperator, QCooperator>
        implements CooperatorRepository {

    private static final QCooperator COOPERATOR = QCooperator.cooperator;

    public CooperatorRepositoryImpl() {
        super(COOPERATOR);
    }
}
