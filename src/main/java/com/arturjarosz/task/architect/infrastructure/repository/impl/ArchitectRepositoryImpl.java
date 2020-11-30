package com.arturjarosz.task.architect.infrastructure.repository.impl;

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.architect.model.QArchitect;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ArchitectRepositoryImpl extends GenericJpaRepositoryImpl<Architect, QArchitect> implements ArchitectRepository {

    private static final QArchitect ARCHITECT = QArchitect.architect;

    protected ArchitectRepositoryImpl() {
        super(ARCHITECT);
    }

}
