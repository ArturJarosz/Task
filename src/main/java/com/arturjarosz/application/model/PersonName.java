package com.arturjarosz.application.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class PersonName extends AbstractValueObject<PersonName> implements Value<PersonName> {
    private static final long serialVersionUID = 8563213541166171011L;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    public PersonName() {
        //needed by Hibernate
    }

    public PersonName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    @Override
    public PersonName copy(PersonName personName) {
        return new PersonName(this.firstName, this.lastName);
    }
}
