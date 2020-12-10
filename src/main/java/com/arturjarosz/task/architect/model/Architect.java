package com.arturjarosz.task.architect.model;

import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.model.PersonName;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ARCHITECT")
public class Architect extends AbstractAggregateRoot {
    private static final long serialVersionUID = -194851694606886763L;

    @Embedded
    private PersonName personName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "PROJECTS_VALUE"))
    private Money projectsValue;

    protected Architect() {
        //needed by Hibernate
    }

    public Architect(String firstName, String lastName) {
        this.projectsValue = new Money(0);
        this.updateArchitectName(firstName, lastName);
    }

    public void updateArchitectName(String firstName, String lastName) {
        PersonName personName = new PersonName(firstName, lastName);
        this.personName = personName;
    }

    //TODO: implement method responsible for updating all projects value

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
