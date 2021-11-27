package com.arturjarosz.task.systemparameter.infrastructure.repository.impl;

import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import com.arturjarosz.task.systemparameter.infrastructure.repository.SystemParameterRepository;
import com.arturjarosz.task.systemparameter.model.QSystemParameter;
import com.arturjarosz.task.systemparameter.model.SystemParameter;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SystemParameterRepositoryImpl extends GenericJpaRepositoryImpl<SystemParameter, QSystemParameter> implements SystemParameterRepository {

    public static final QSystemParameter SYSTEM_PARAMETER = QSystemParameter.systemParameter;

    public SystemParameterRepositoryImpl() {
        super(SYSTEM_PARAMETER);
    }

}
