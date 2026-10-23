package com.arturjarosz.task.systemparameter.query.impl;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.systemparameter.application.SystemParameterDtoMapper;
import com.arturjarosz.task.systemparameter.model.QSystemParameter;
import com.arturjarosz.task.systemparameter.query.SystemParameterQueryService;
import com.querydsl.core.types.Projections;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Finder
public class SystemParameterQueryServiceImpl extends AbstractQueryService<QSystemParameter> implements SystemParameterQueryService {

    private static final QSystemParameter SYSTEM_PARAMETER = QSystemParameter.systemParameter;

    public SystemParameterQueryServiceImpl() {
        super(SYSTEM_PARAMETER);
    }

    @Override
    public SystemParameterDto getSystemPropertyByName(String name) {
        var systemParameterDto = this.queryFromAggregate().where(SYSTEM_PARAMETER.name.eq(name))
                .select(Projections.bean(SystemParameterDto.class, SYSTEM_PARAMETER.id, SYSTEM_PARAMETER.name,
                        SYSTEM_PARAMETER.type.stringValue().as("type"), SYSTEM_PARAMETER.value,
                        SYSTEM_PARAMETER.defaultValue, SYSTEM_PARAMETER.singleValue)).fetchOne();
        return SystemParameterDtoMapper.hideSecretValue(systemParameterDto);
    }

    @Override
    public String getValueByName(String name) {
        return this.queryFromAggregate().where(SYSTEM_PARAMETER.name.eq(name)).select(SYSTEM_PARAMETER.value).fetchOne();
    }

    @Override
    public Map<String, String> getValuesByNames(Collection<String> names) {
        // values can be null, which Collectors.toMap does not accept
        var values = new HashMap<String, String>();
        this.queryFromAggregate().where(SYSTEM_PARAMETER.name.in(names))
                .select(SYSTEM_PARAMETER.name, SYSTEM_PARAMETER.value).fetch()
                .forEach(tuple -> values.put(tuple.get(SYSTEM_PARAMETER.name), tuple.get(SYSTEM_PARAMETER.value)));
        return values;
    }

    @Override
    public SystemParameterDto getSystemParameter(long systemParameterId) {
        var systemParameterDto = this.queryFromAggregate().where(SYSTEM_PARAMETER.id.eq(systemParameterId))
                .select(Projections.bean(SystemParameterDto.class, SYSTEM_PARAMETER.id, SYSTEM_PARAMETER.name,
                        SYSTEM_PARAMETER.type.stringValue().as("type"), SYSTEM_PARAMETER.value,
                        SYSTEM_PARAMETER.defaultValue, SYSTEM_PARAMETER.singleValue)).fetchOne();
        return SystemParameterDtoMapper.hideSecretValue(systemParameterDto);
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
                        SYSTEM_PARAMETER.defaultValue, SYSTEM_PARAMETER.singleValue)).fetch()
                .stream()
                .map(SystemParameterDtoMapper::hideSecretValue)
                .toList();
    }
}
