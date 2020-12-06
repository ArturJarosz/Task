package com.arturjarosz.task.client.application.dto;

import com.arturjarosz.task.sharedkernel.model.Money;

import java.io.Serializable;

public class ClientDataDto implements Serializable {
    private static final long serialVersionUID = 6068972434951600297L;

    private String email;
    private String telephone;
    private AddressDto address;
    private String note;
    private Money projectsValue;

    public ClientDataDto(String email, String telephone, AddressDto address, String note, Money projectsValue) {
        this.email = email;
        this.telephone = telephone;
        this.address = address;
        this.note = note;
        this.projectsValue = projectsValue;
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

    public AddressDto getAddress() {
        return this.address;
    }

    public void setAddress(AddressDto address) {
        this.address = address;
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Money getProjectsValue() {
        return this.projectsValue;
    }

    public void setProjectsValue(Money projectsValue) {
        this.projectsValue = projectsValue;
    }
}
