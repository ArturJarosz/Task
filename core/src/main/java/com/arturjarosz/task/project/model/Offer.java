package com.arturjarosz.task.project.model;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "OFFER")
public class Offer extends Arrangement {
    private static final long serialVersionUID = -3315754399242136650L;

    @Column(name = "IS_OFFER_ACCEPTED")
    private boolean isAccepted;

    protected Offer() {
        //needed by Hibernate
    }

    public Offer(double offerValue) {
        super(offerValue);
        this.isAccepted = false;
    }

    public boolean isAccepted() {
        return this.isAccepted;
    }

    public void acceptOffer() {
        this.isAccepted = true;
    }
}
