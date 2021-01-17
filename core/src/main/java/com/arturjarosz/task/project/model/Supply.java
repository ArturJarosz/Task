package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supply_sequence", allocationSize = 1)
@Table(name = "SUPPLY")
public class Supply extends AbstractEntity {

    private static final long serialVersionUID = -8876043250299302884L;
    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "SUPPLIER_ID", nullable = false)
    private Long supplierId;

    @Embedded
    private Money value;

}
