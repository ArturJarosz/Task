package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
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
@SequenceGenerator(name = "sequence_generator", sequenceName = "cooperator_job", allocationSize = 1)
@Table(name = "COOPERATOR_JOB")
public class CooperatorJob extends AbstractEntity {
    private static final long serialVersionUID = -2817735161319438104L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE"))
    private Money value;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "COOPERATOR_ID", nullable = false)
    private Long cooperatorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private CooperatorJobType type;

    public CooperatorJob(String name, Long cooperatorId) {
        this.name = name;
        this.cooperatorId = cooperatorId;
    }

    public String getName() {
        return this.name;
    }

    public Double getValue() {
        return this.value.getValue().doubleValue();
    }

    public String getNote() {
        return this.note;
    }

    public Long getCooperatorId() {
        return this.cooperatorId;
    }

    public CooperatorJobType getType() {
        return this.type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setValue(Double value) {
        this.value = new Money(value);
    }

    public void setNote(String note) {
        this.note = note;
    }
}
