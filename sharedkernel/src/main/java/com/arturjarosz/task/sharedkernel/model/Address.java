package com.arturjarosz.task.sharedkernel.model;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;

/**
 * Class that represents an address.
 */

@Embeddable
public class Address extends AbstractValueObject<Address> implements ValueObject<Address> {
    @Serial
    private static final long serialVersionUID = -4102560398759336232L;

    @Column(name = "POST_CODE")
    private String postCode;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STREET")
    private String street;

    public Address() {
        // needed by JPA
    }

    public Address(String postCode, String city, String street) {
        this.postCode = postCode;
        this.city = city;
        this.street = street;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        assertNotEmpty(city, BaseValidator
                .createMessageCode(ExceptionCodes.NULL, ModelExceptionCodes.ADDRESS,
                        ModelExceptionCodes.CITY));
        this.city = city;
    }

    public String getStreet() {
        return this.street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.city)
                .append(this.postCode)
                .append(this.street)
                .toHashCode();
    }

    @Override
    public boolean hasSameValueAs(Address other) {
        return new EqualsBuilder()
                .append(this.city, other.city)
                .append(this.postCode, other.postCode)
                .append(this.street, other.street)
                .isEquals();
    }

    @Override
    public Address copy() {
        return new Address(this.postCode, this.city, this.street);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Address address)) return false;

        return this.hasSameValueAs(address);
    }
}
