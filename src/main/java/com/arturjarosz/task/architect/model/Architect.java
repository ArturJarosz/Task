package com.arturjarosz.task.architect.model;

import com.arturjarosz.task.architect.domain.ArchitectExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.model.PersonName;

import javax.persistence.*;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;
import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.createMessageCode;

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

    public Architect(PersonName personName) {
        this.projectsValue = new Money(0);
        this.updateArchitectName(personName);
    }

    public void updateArchitectName(PersonName personName) {
        assertIsTrue(personName != null,
                createMessageCode(ExceptionCodes.IS_NULL, ArchitectExceptionCodes.ARCHITECT,
                        ArchitectExceptionCodes.PERSON_NAME));
        this.personName = personName;
    }

    //TODO: implement method responsible for updating all projects value

}
