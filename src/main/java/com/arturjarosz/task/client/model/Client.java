package com.arturjarosz.task.client.model;

import com.arturjarosz.task.sharedkernel.model.*;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "CLIENT")
public class Client extends AbstractAggregateRoot {

    private static final long serialVersionUID = 5821492165714199395L;

    /* TODO: implement reference to Project by ids - its a separate Aggregate - value should be updated
         everytime a client is chosen in Project and Client removed from the project*/

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

    @Column(name = "CLIENT_TYPE")
    private ClientType clientType;

    //TODO: implement meeting with clients field, when that entity is ready

    //TODO: implement projects field, when that entity is ready

    protected Client() {
        //needed by Hibernate
    }

    public Client(PersonName personName, String companyName,
                  Address address, Email email, String note, String telephone,
                  ClientType clientType) {
        this.projectsValue = this.projectsValue;
        this.personName = personName;
        this.companyName = companyName;
        this.address = address;
        this.email = email;
        this.note = note;
        this.telephone = telephone;
        this.clientType = clientType;
        this.projectsValue = new Money(0);
    }

    public void updateProjectsValue() {
        //TODO: update this method after Project entity and table is fully implemented, for now it is 0
        this.projectsValue.setValue(new BigDecimal(0));
    }

    public static Client createPrivateClient(PersonName personName, Address address, Email email, String note,
                                             String telephone) {
        Client client = new Client(personName, null, address, email, note, telephone, ClientType.PRIVATE);
        return client;
    }

    public static Client createCorporateClient(String companyName, Address address, Email email, String note,
                                               String telephone) {
        Client client = new Client(null, companyName, address, email, note, telephone, ClientType.CORPORATE);
        return client;
    }
}
