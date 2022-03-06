package com.arturjarosz.task.supplier.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Email;

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

    @Column(name = "NAME", updatable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false)
    private SupplierCategory category;

    @Embedded
    private Email email;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Column(name = "NOTE")
    private String note;

    protected Supplier() {
        // needed by Hibernate
    }

    public Supplier(String name, SupplierCategory category) {
        this.name = name;
        this.category = category;
    }

    public void update(String name, SupplierCategory category, String email, String telephone, String note) {
        this.name = name;
        this.category = category;
        this.email = new Email(email);
        this.telephone = telephone;
        this.note = note;
    }

    public String getName() {
        return this.name;
    }

    public SupplierCategory getCategory() {
        return this.category;
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
}
