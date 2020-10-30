package com.arturjarosz.application.model;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CLIENT")
public class Client extends AbstractPerson {
    private static final long serialVersionUID = -8962377833920420934L;

    @Embedded
    private Address address;

    @Embedded
    private Email email;

    public Client() {
        //needed by Hibernate
    }

    public Client(PersonName name) {
        super(name);
    }

    public Address getAddress() {
        return this.address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public Email getEmail() {
        return this.email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }
}
