package com.arturjarosz.application.model;

public class Architect extends AbstractPerson {
    private static final long serialVersionUID = -194851694606886763L;

    public Architect() {
        //needed by Hibernate
    }

    public Architect(PersonName name) {
        super(name);
    }

}
