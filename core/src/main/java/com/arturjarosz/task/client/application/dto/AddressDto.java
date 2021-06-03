package com.arturjarosz.task.client.application.dto;

import java.io.Serializable;

public class AddressDto implements Serializable {
    private static final long serialVersionUID = 6200531143119979541L;

    private String city;
    private String postCode;
    private String street;
    private String houseNumber;
    private String flatNumber;

    public AddressDto() {
        //needed by Hibernate
    }

    public String getCity() {
        return this.city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getPostCode() {
        return this.postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
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
}
