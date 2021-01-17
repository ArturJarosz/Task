package com.arturjarosz.task.constructor.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Email;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(initialValue = 1, name = "idgen")
@Table(name = "CONSTRUCTOR")
public class Constructor extends AbstractAggregateRoot {
    private static final long serialVersionUID = -4555725640264137537L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ConstructorType constructorType;

    @Column(name = "NOTE")
    private String note;

    @Embedded
    private Email email;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "JOBS_VALUE"))
    private Money jobsValue;

}
