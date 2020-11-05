package com.arturjarosz.application.model;

import com.arturjarosz.application.exceptions.BaseValidator;
import com.arturjarosz.application.exceptions.ExceptionCodes;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import static com.arturjarosz.application.exceptions.BaseValidator.assertNotEmpty;

@Embeddable
public class PersonName extends AbstractValueObject<PersonName> implements ValueObject<PersonName> {
    private static final long serialVersionUID = 8563213541166171011L;

    @Column(name = "FIRST_NAME")
    private String firstName;

    @Column(name = "LAST_NAME")
    private String lastName;

    public PersonName() {
        //needed by Hibernate
    }

    public PersonName(String firstName, String lastName) {
        this.setFirstName(firstName);
        this.lastName = lastName;
    }

    private void setFirstName(String firstName) {
        assertNotEmpty(firstName,
                BaseValidator.createMessageCode(ExceptionCodes.IS_NULL, ModelExceptionCodes.FIRST_NAME));
    }

    private void setLastName(String lastName) {
        assertNotEmpty(lastName,
                BaseValidator.createMessageCode(ExceptionCodes.IS_NULL, ModelExceptionCodes.LAST_NAME));
    }

    public String getFirstName() {
        return this.firstName;
    }

    public String getLastName() {
        return this.lastName;
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
    public PersonName copy(PersonName personName) {
        return new PersonName(this.getFirstName(), this.getLastName());
    }
}
