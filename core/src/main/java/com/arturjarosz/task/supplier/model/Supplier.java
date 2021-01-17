package com.arturjarosz.task.supplier.model;

import com.arturjarosz.task.contractor.model.ContractorType;
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
@SequenceGenerator(name = "sequence_generator", sequenceName = "supplier_sequence", allocationSize = 1)
@Table(name = "SUPPLIER")
public class Supplier extends AbstractAggregateRoot {
    private static final long serialVersionUID = 8869912706093861909L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ContractorType contractorType;

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
