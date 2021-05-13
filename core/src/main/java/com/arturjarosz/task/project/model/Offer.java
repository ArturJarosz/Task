package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractValueObject;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.model.ValueObject;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;

@Embeddable
public class Offer extends AbstractValueObject<Offer> implements ValueObject<Offer>, Comparable<Offer> {

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "OFFER_VALUE"))
    private Money offerValue;

    @Column(name = "IS_OFFER_ACCEPTED")
    private boolean isAccepted;

    protected Offer() {
        //needed by Hibernate
    }

    public Offer(double offerValue) {
        this.offerValue = new Money(offerValue);
        this.isAccepted = false;
    }

    public Money getOfferValue() {
        return this.offerValue;
    }

    public boolean isAccepted() {
        return this.isAccepted;
    }

    public void accept() {
        this.isAccepted = true;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.offerValue).append(this.isAccepted).toHashCode();
    }

    @Override
    public boolean hasSameValueAs(Offer other) {
        return new EqualsBuilder()
                .append(this.offerValue, other.offerValue)
                .append(this.isAccepted, other.isAccepted)
                .isEquals();
    }

    @Override
    public Offer copy() {
        return new Offer(this.offerValue.getValue().doubleValue());
    }

    @Override
    public int compareTo(Offer offer) {
        return this.offerValue.compareTo(offer.offerValue);
    }
}
