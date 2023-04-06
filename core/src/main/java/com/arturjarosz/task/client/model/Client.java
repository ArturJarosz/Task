package com.arturjarosz.task.client.model;

import com.arturjarosz.task.sharedkernel.model.*;

import javax.persistence.*;
import java.io.Serial;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "client_sequence", allocationSize = 1)
@Table(name = "CLIENT")
public class Client extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = 5821492165714199395L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "PROJECTS_VALUE"))
    private Money projectsValue;

    @Embedded
    private PersonName personName;

    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Embedded
    private Address address;

    @Embedded
    private Email email;

    @Column(name = "NOTE")
    private String note;

    @Column(name = "TELEPHONE")
    private String telephone;

    @Enumerated(EnumType.STRING)
    @Column(name = "CLIENT_TYPE")
    private ClientType clientType;

    protected Client() {
        // needed by JPA
    }

    public Client(PersonName personName, String companyName, ClientType clientType) {
        this.personName = personName;
        this.companyName = companyName;
        this.clientType = clientType;
        this.projectsValue = new Money(0);
    }

    public static Client createPrivateClient(String firstName, String lastName) {
        return new Client(new PersonName(firstName, lastName), null, ClientType.PRIVATE);
    }

    public static Client createCorporateClient(String companyName) {
        return new Client(null, companyName, ClientType.CORPORATE);
    }

    public Email getEmail() {
        return this.email;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public PersonName getPersonName() {
        return this.personName;
    }

    public String getCompanyName() {
        return this.companyName;
    }

    public boolean isPrivate() {
        return this.clientType.equals(ClientType.PRIVATE);
    }

    public boolean isCorporate() {
        return this.clientType.equals(ClientType.CORPORATE);
    }

    public Address getAddress() {
        return this.address;
    }

    public Money getProjectsValue() {
        return this.projectsValue;
    }

    public String getNote() {
        return this.note;
    }

    public String getTelephone() {
        return this.telephone;
    }

    public void updateProjectsValue(Money newValue) {
        this.projectsValue = newValue.copy();
    }

    public void updatePersonName(String firstName, String lastName) {
        this.personName.setFirstName(firstName);
        this.personName.setLastName(lastName);
    }

    public void updateCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public void updateNote(String note) {
        this.note = note;
    }

    public void updateTelephone(String telephone) {
        this.telephone = telephone;
    }

    public void updateAddress(Address address) {
        this.address = address.copy();
    }

    public void updateEmail(String email) {
        this.email = new Email(email);
    }

}
