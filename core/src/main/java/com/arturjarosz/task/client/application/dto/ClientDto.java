package com.arturjarosz.task.client.application.dto;

import com.arturjarosz.task.client.model.ClientType;

import java.io.Serializable;

public class ClientDto implements Serializable {
    private static final long serialVersionUID = 5521050841435723450L;

    private Long id;
    private String firstName;
    private String lastName;
    private String companyName;
    private ContactDto contact;
    private ClientAdditionalDataDto additionalData;
    private ClientType clientType;

    public ClientDto() {
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

    public String getCompanyName() {
        return this.companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public ContactDto getContact() {
        return this.contact;
    }

    public void setContact(ContactDto contact) {
        this.contact = contact;
    }

    public ClientAdditionalDataDto getAdditionalData() {
        return this.additionalData;
    }

    public void setAdditionalData(ClientAdditionalDataDto additionalData) {
        this.additionalData = additionalData;
    }

    public ClientType getClientType() {
        return this.clientType;
    }

    public void setClientType(ClientType clientType) {
        this.clientType = clientType;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
