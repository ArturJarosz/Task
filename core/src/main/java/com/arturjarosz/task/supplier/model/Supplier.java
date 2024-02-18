package com.arturjarosz.task.supplier.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Email;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "supplier_sequence", allocationSize = 1)
@Table(name = "SUPPLIER")
public class Supplier extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = 6745432422347427647L;
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
        // needed by JPA
    }

    public Supplier(String name, SupplierCategory category, String email, String telephone, String note) {
        this.name = name;
        this.category = category;
        this.email = new Email(email);
        this.telephone = telephone;
        this.note = note;
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
