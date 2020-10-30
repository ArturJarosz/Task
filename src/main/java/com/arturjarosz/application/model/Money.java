package com.arturjarosz.application.model;

import javax.persistence.Column;

public class Money extends AbstractEntity {
    private static final long serialVersionUID = -5524298857488493145L;

    @Column(name = "MONEY")
    private String value;

    public Money() {
        //needed by Hibernate
    }

    public Money(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
