package com.arturjarosz.task.architect.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.model.PersonName;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

import java.io.Serial;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "architect_sequence", allocationSize = 1)
@Table(name = "ARCHITECT")
public class Architect extends AbstractAggregateRoot {
    @Serial
    private static final long serialVersionUID = -194851694606886763L;

    @Embedded
    private PersonName personName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "PROJECTS_VALUE"))
    private Money projectsValue;

    protected Architect() {
        // needed by JPA
    }

    public Architect(String firstName, String lastName) {
        this.projectsValue = new Money(0);
        this.updateArchitectName(firstName, lastName);
    }

    public void updateArchitectName(String firstName, String lastName) {
        this.personName = new PersonName(firstName, lastName);
    }

    public PersonName getPersonName() {
        return this.personName;
    }

    public void setPersonName(PersonName personName) {
        this.personName = personName;
    }

    public Money getProjectsValue() {
        return this.projectsValue;
    }

    public void setProjectsValue(Money projectsValue) {
        this.projectsValue = projectsValue;
    }
}
