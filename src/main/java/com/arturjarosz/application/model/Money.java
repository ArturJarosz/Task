package com.arturjarosz.application.model;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

@Embeddable
public class Money extends AbstractValueObject<Money> implements ValueObject<Money> {
    private static final long serialVersionUID = -5524298857488493145L;

    @Column(name = "MONEY")
    private BigDecimal value;

    public Money() {
        //needed by Hibernate
    }

    public Money(BigDecimal value) {
        this.value = value;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    @Override
    public boolean hasSameValueAs(Money other) {
        return false;
    }

    @Override
    public Money copy(Money money) {
        return null;
    }
}
