package com.arturjarosz.application.model;

import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ARCHITECT")
public class Architect extends AbstractPerson {
    private static final long serialVersionUID = -194851694606886763L;

    public Architect() {
        //needed by Hibernate
    }

    public Architect(PersonName name) {
        super(name);
    }

}
