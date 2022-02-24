package com.arturjarosz.task.cooperator.model;

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
@SequenceGenerator(name = "sequence_generator", sequenceName = "cooperator_sequence", allocationSize = 1)
@Table(name = "COOPERATOR")
public class Cooperator extends AbstractAggregateRoot {
    private static final long serialVersionUID = -1662769005230262558L;

    @Column(name = "NAME", updatable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    private CooperatorType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false)
    private CooperatorCategory category;

    @Embedded
    private Email email;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Column(name = "NOTE")
    private String note;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE"))
    private Money value;

    protected Cooperator() {
        //needed by Hibernate
    }

    protected Cooperator(String name, CooperatorType type, CooperatorCategory category) {
        this.name = name;
        this.type = type;
        this.category = category;
        this.value = new Money(0.0);
    }

    public static Cooperator createSupplier(String name, CooperatorCategory.SupplierCategory category) {
        return new Cooperator(name, CooperatorType.SUPPLIER, category.asCooperatorCategory());
    }

    public static Cooperator createContractor(String name, CooperatorCategory.ContractorCategory category) {
        return new Cooperator(name, CooperatorType.CONTRACTOR, category.asCooperatorCategory());
    }

    public CooperatorCategory getCategory() {
        return this.category;
    }

    public CooperatorType getType() {
        return this.type;
    }

    public void update(String name, CooperatorCategory category, String email, String telephone, String note) {
        this.name = name;
        this.category = category;
        this.email = new Email(email);
        this.telephone = telephone;
        this.note = note;
    }

    public String getName() {
        return this.name;
    }

    public String getEmail() {
        if (this.email == null) {
            return null;
        }
        return this.email.getValue();
    }

    public String getTelephone() {
        return this.telephone;
    }

    public String getNote() {
        return this.note;
    }

    public Double getValue() {
        return this.value.getValue().doubleValue();
    }
}
