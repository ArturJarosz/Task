package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.Column;
import javax.persistence.Embedded;

public class ConstructorJob extends AbstractEntity {

    private static final long serialVersionUID = -3479822204290674024L;
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "CONSTRUCTOR_ID", nullable = false)
    private Long constructorId;

    @Embedded
    private Money value;
}
