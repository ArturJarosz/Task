package com.arturjarosz.task.contractor.model;

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
@SequenceGenerator(name = "sequence_generator", sequenceName = "contractor_sequence", allocationSize = 1)
@Table(name = "CONTRACTOR")
public class Contractor extends AbstractAggregateRoot {


    @Column(name = "NAME", updatable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "CATEGORY", nullable = false)
    private ContractorCategory category;

    @Embedded
    private Email email;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Column(name = "NOTE")
    private String note;

    protected Contractor() {
        // needed by JPA
    }

    public Contractor(String name, ContractorCategory category) {
        this.name = name;
        this.category = category;
    }

    public void update(String name, ContractorCategory category, String email, String telephone, String note) {
        this.name = name;
        this.category = category;
        this.email = new Email(email);
        this.telephone = telephone;
        this.note = note;
    }

    public String getName() {
        return this.name;
    }

    public ContractorCategory getCategory() {
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
