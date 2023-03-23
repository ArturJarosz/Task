package com.arturjarosz.task.sharedkernel.model;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertNotEmpty;

/**
 * Class that represents an address.
 */

@Embeddable
public class Address extends AbstractValueObject<Address> implements ValueObject<Address> {
    private static final long serialVersionUID = -4102560398759336232L;

    @Column(name = "POST_CODE")
    private String postCode;

    @Column(name = "CITY")
    private String city;

    @Column(name = "STREET")
    private String street;

    @Column(name = "HOUSE_NUMBER")
    private String houseNumber;

    @Column(name = "FLAT_NUMBER")
    private String flatNumber;

    public Address() {
        // needed by JPA
    }

    public Address(String postCode, String city, String street, String houseNumber, String flatNumber) {
        this.postCode = postCode;
        this.city = city;
        this.street = street;
        this.houseNumber = houseNumber;
        this.flatNumber = flatNumber;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
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

    public String getHouseNumber() {
        return this.houseNumber;
    }

    public void setHouseNumber(String houseNumber) {
        this.houseNumber = houseNumber;
    }

    public String getFlatNumber() {
        return this.flatNumber;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.city)
                .append(this.postCode)
                .append(this.street)
                .append(this.houseNumber)
                .append(this.flatNumber)
                .toHashCode();
    }

    @Override
    public boolean hasSameValueAs(Address other) {
        return new EqualsBuilder()
                .append(this.city, other.city)
                .append(this.postCode, other.postCode)
                .append(this.street, other.street)
                .append(this.houseNumber, other.houseNumber)
                .append(this.flatNumber, other.flatNumber)
                .isEquals();
    }

    @Override
    public Address copy() {
        return new Address(this.postCode, this.city, this.street, this.houseNumber, this.flatNumber);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof Address)) return false;

        Address address = (Address) o;

        return this.hasSameValueAs(address);
    }
}
