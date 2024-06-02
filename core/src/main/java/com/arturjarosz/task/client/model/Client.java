package com.arturjarosz.task.client.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Address;
import com.arturjarosz.task.sharedkernel.model.Email;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.model.PersonName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.envers.Audited;

import java.io.Serial;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "client_sequence", allocationSize = 1)
@Table(name = "CLIENT")
public class Client extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = 5821492165714199395L;

    @Getter
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "PROJECTS_VALUE"))
    private Money projectsValue;

    @Audited
    @Setter
    @Getter
    @Embedded
    private PersonName personName;

    @Audited
    @Setter
    @Getter
    @Column(name = "COMPANY_NAME")
    private String companyName;

    @Audited
    @Getter
    @Setter
    @Embedded
    private Address address;

    @Audited
    @Getter
    @Setter
    @Embedded
    private Email email;

    @Audited
    @Getter
    @Setter
    @Column(name = "NOTE")
    private String note;

    @Audited
    @Getter
    @Setter
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

    public boolean isPrivate() {
        return this.clientType.equals(ClientType.PRIVATE);
    }

    public boolean isCorporate() {
        return this.clientType.equals(ClientType.CORPORATE);
    }


    public void setPersonName(String firstName, String lastName) {
        this.personName.setFirstName(firstName);
        this.personName.setLastName(lastName);
    }


    public void updateEmail(String email) {
        this.email = new Email(email);
    }

}
