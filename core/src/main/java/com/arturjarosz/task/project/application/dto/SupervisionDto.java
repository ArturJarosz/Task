package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SupervisionDto implements Serializable {
    private static final long serialVersionUID = 6120237413436097370L;

    private Long id;
    private boolean hasInvoice;
    private BigDecimal baseNetRate;
    private BigDecimal hourlyNetRate;
    private BigDecimal visitNetRate;

    public SupervisionDto() {
        //needed by Hibernate
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public boolean isHasInvoice() {
        return this.hasInvoice;
    }

    public void setHasInvoice(boolean hasInvoice) {
        this.hasInvoice = hasInvoice;
    }

    public BigDecimal getBaseNetRate() {
        return this.baseNetRate;
    }

    public void setBaseNetRate(BigDecimal baseNetRate) {
        this.baseNetRate = baseNetRate;
    }

    public BigDecimal getHourlyNetRate() {
        return this.hourlyNetRate;
    }

    public void setHourlyNetRate(BigDecimal hourlyNetRate) {
        this.hourlyNetRate = hourlyNetRate;
    }

    public BigDecimal getVisitNetRate() {
        return this.visitNetRate;
    }

    public void setVisitNetRate(BigDecimal visitNetRate) {
        this.visitNetRate = visitNetRate;
    }
}
