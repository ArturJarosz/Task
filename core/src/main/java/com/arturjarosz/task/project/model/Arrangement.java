package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "arrangement_sequence", allocationSize = 1)
@DiscriminatorColumn(name = "ARRANGEMENT_TYPE", discriminatorType = DiscriminatorType.STRING)
@Table(name = "ARRANGEMENT")
public class Arrangement extends AbstractEntity {
    private static final long serialVersionUID = -5202333655625198483L;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "VALUE"))
    private Money offerValue;

    protected Arrangement() {
        // needed by Hibernate
    }

    public Arrangement(double offerValue) {
        this.offerValue = new Money(offerValue);
    }

    public Money getOfferValue() {
        return this.offerValue;
    }

    public void makeNewOffer(Double offerValue) {
        this.offerValue = new Money(offerValue);
    }

}
