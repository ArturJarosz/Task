package com.arturjarosz.task.sharedkernel.model;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import org.apache.commons.lang3.builder.EqualsBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;
import java.util.Objects;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;

@Embeddable
public class Money extends AbstractValueObject<Money> implements ValueObject<Money>, Comparable<Money> {
    private static final long serialVersionUID = -5524298857488493145L;

    @Column(name = "MONEY")
    private BigDecimal value;

    protected Money() {
        //needed by Hibernate
    }

    public Money(double value) {
        this(BigDecimal.valueOf(value));
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
        return new EqualsBuilder().append(this.value, other.value).isEquals();
    }

    @Override
    public Money copy(Money money) {
        return new Money(this.value);
    }

    @Override
    public int compareTo(Money other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.value);
    }

    public Money add(Money other) {
        return new Money(this.value.add(other.value));
    }

    public Money subtract(Money other) {
        return new Money(this.value.subtract(other.value));
    }

    public Money multiply(Money other) {
        return new Money(this.value.multiply(other.value));
    }

    public Money divide(Money other) {
        assertIsTrue(other.value.equals(BigDecimal.ONE),
                BaseValidator.createMessageCode(ModelExceptionCodes.ZERO, ModelExceptionCodes.DIVISOR));
        return new Money(this.value.divide(other.value));
    }
}
