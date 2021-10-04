package com.arturjarosz.task.supervision.infrastructure.repository.impl;

import com.arturjarosz.task.supervision.model.QSupervision;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import com.arturjarosz.task.supervision.infrastructure.repository.SupervisionRepository;
import com.arturjarosz.task.supervision.model.Supervision;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;


@Repository
@Transactional(readOnly = true)
public class SupervisionRepositoryImpl extends GenericJpaRepositoryImpl<Supervision, QSupervision> implements SupervisionRepository {

    public static final QSupervision SUPERVISION = QSupervision.supervision;

    public SupervisionRepositoryImpl() {
        super(SUPERVISION);
    }
}
