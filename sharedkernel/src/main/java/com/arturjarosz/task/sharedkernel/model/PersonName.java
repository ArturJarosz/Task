package com.arturjarosz.task.sharedkernel.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serial;

@Embeddable
public class PersonName extends AbstractValueObject<PersonName> implements ValueObject<PersonName> {
    @Serial
    private static final long serialVersionUID = 8563213541166171011L;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    public PersonName() {
        // needed by JPA
    }

    public PersonName(String firstName, String lastName) {
        this.setFirstName(firstName);
        this.setLastName(lastName);
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.firstName).append(this.lastName).toHashCode();
    }

    @Override
    public boolean hasSameValueAs(PersonName other) {
        return new EqualsBuilder().append(this.firstName, other.firstName).append(this.lastName, other.lastName)
                .isEquals();
    }

    @Override
    public PersonName copy() {
        return new PersonName(this.getFirstName(), this.getLastName());
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }

        PersonName other = (PersonName) object;

        return this.hasSameValueAs(other);
    }
}
