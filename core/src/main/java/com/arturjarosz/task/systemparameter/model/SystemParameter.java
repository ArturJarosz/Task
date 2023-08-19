package com.arturjarosz.task.systemparameter.model;

import com.arturjarosz.task.dto.SystemParameterDto;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "system_parameter_sequence", allocationSize = 1)
@Table(name = "SYSTEM_PARAMETER")
public class SystemParameter extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = 9194763340793223514L;

    @Column(name = "NAME")
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE")
    private SystemParameterType type;

    @Column(name = "VALUE")
    private String value;

    @Column(name = "DEFAULT_VALUE")
    private String defaultValue;

    @Column(name = "SINGLE_VALUE")
    private boolean singleValue;

    protected SystemParameter() {
        // needed by JPA
    }

    public SystemParameter(String name, String value, String defaultValue, SystemParameterType type,
            boolean singleValue) {
        this.name = name;
        this.value = value;
        this.defaultValue = defaultValue;
        this.type = type;
        this.singleValue = singleValue;
    }


    public void update(SystemParameterDto systemParameterDto) {
        this.value = systemParameterDto.getValue();
    }

    public String getName() {
        return this.name;
    }

    public SystemParameterType getType() {
        return this.type;
    }

    public String getValue() {
        return this.value;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public boolean isSingleValue() {
        return this.singleValue;
    }
}
