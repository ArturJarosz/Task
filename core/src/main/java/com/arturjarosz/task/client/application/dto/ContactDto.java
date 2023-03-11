package com.arturjarosz.task.client.application.dto;

import java.io.Serializable;

public class ContactDto implements Serializable {
    private static final long serialVersionUID = -2827890153515315184L;

    private AddressDto address;
    private String email;
    private String telephone;

    public ContactDto() {
        // needed by JPA
    }

    public AddressDto getAddress() {
        return this.address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }
}
