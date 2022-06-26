package com.arturjarosz.task.systemparameter.query.impl;

import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.systemparameter.domain.dto.SystemParameterDto;
import com.arturjarosz.task.systemparameter.model.QSystemParameter;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import com.querydsl.core.types.Projections;

import java.util.List;

@Finder
public class SystemParameterQueryServiceImpl extends AbstractQueryService<QSystemParameter> implements SystemParameterQueryService {

    private static final QSystemParameter SYSTEM_PARAMETER = QSystemParameter.systemParameter;

    public SystemParameterQueryServiceImpl() {
        super(SYSTEM_PARAMETER);
    }

    @Override
    public SystemParameterDto getSystemPropertyByName(String name) {
        return this.queryFromAggregate().where(SYSTEM_PARAMETER.name.eq(name))
                .select(Projections.bean(SystemParameterDto.class, SYSTEM_PARAMETER.id, SYSTEM_PARAMETER.name,
                        SYSTEM_PARAMETER.type.stringValue().as("type"), SYSTEM_PARAMETER.value,
                        SYSTEM_PARAMETER.defaultValue, SYSTEM_PARAMETER.singleValue)).fetchOne();
    }

    @Override
    public SystemParameterDto getSystemParameter(long systemParameterId) {
        return this.queryFromAggregate().where(SYSTEM_PARAMETER.id.eq(systemParameterId))
                .select(Projections.bean(SystemParameterDto.class, SYSTEM_PARAMETER.id, SYSTEM_PARAMETER.name,
                        SYSTEM_PARAMETER.type.stringValue().as("type"), SYSTEM_PARAMETER.value,
                        SYSTEM_PARAMETER.defaultValue, SYSTEM_PARAMETER.singleValue)).fetchOne();
    }

    @Override
    public List<String> getSystemParametersNames() {
        return this.queryFromAggregate().select(SYSTEM_PARAMETER.name).fetch();
    }

    @Override
    public List<SystemParameterDto> getSystemParameters() {
        return this.queryFromAggregate()
                .select(Projections.bean(SystemParameterDto.class, SYSTEM_PARAMETER.id, SYSTEM_PARAMETER.name,
                        SYSTEM_PARAMETER.type.stringValue().as("type"), SYSTEM_PARAMETER.value,
                        SYSTEM_PARAMETER.defaultValue, SYSTEM_PARAMETER.singleValue)).fetch();
    }
}
