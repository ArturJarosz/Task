package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;

public class OfferDto implements Serializable {
    private static final long serialVersionUID = 2871016534140942045L;

    private Double offerValue;
    private Boolean isAccepted;

    public OfferDto() {
        //needed by Hibernate
    }

    public Double getOfferValue() {
        return this.offerValue;
    }

    public void setOfferValue(Double offerValue) {
        this.offerValue = offerValue;
    }

    public Boolean getAccepted() {
        return this.isAccepted;
    }

    public void setAccepted(Boolean accepted) {
        this.isAccepted = accepted;
    }
}
