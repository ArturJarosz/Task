package com.arturjarosz.task.architect.infrastructure.repository.impl;

import com.arturjarosz.task.architect.infrastructure.repository.ArchitectRepository;
import com.arturjarosz.task.architect.model.Architect;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.AbstractBaseRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ArchitectRepositoryImpl extends AbstractBaseRepositoryImpl<Architect> implements ArchitectRepository {

}
