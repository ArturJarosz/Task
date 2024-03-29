package com.arturjarosz.task.sharedkernel.model;

import com.arturjarosz.task.sharedkernel.exceptions.BaseValidator;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.math.BigDecimal;
import java.math.RoundingMode;

import static com.arturjarosz.task.sharedkernel.exceptions.BaseValidator.assertIsTrue;

/**
 * Class to represent money values. For not it has only a value, but it meant to be used along with
 * currency. Any arithmetic expressions should be only possible when currency is same on both objects.
 */

@Embeddable
public class Money extends AbstractValueObject<Money> implements ValueObject<Money>, Comparable<Money> {
    @Serial
    private static final long serialVersionUID = -5524298857488493145L;

    @Column(name = "MONEY", precision = 5, scale = 2)
    private BigDecimal value;

    protected Money() {
        // needed by JPA
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
    public Money copy() {
        return new Money(this.value);
    }

    @Override
    public int compareTo(Money other) {
        return this.value.compareTo(other.value);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.value).toHashCode();
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
        return new Money(this.value.divide(other.value, RoundingMode.HALF_UP));
    }

    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }
        if (object == this) {
            return true;
        }
        if (object.getClass() != this.getClass()) {
            return false;
        }

        Money other = (Money) object;

        return this.hasSameValueAs(other);

    }
}
