package com.arturjarosz.task.project.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class SupervisionDto implements Serializable {
    private static final long serialVersionUID = 6120237413436097370L;

    public String ID = "id";
    public String HAS_INVOICE = "hasInvoice";
    public String BASE_NET_RATE = "baseNetRate";
    public String HOURLY_NET_RATE = "hourlyNetRate";
    public String VISIT_NET_RATE = "visitNetRate";

    private Long id;
    private Long projectId;
    private boolean hasInvoice;
    private BigDecimal baseNetRate;
    private BigDecimal hourlyNetRate;
    private BigDecimal visitNetRate;
    private int hoursCount;

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

    public Long getProjectId() {
        return this.projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
    }

    public int getHoursCount() {
        return this.hoursCount;
    }

    public void setHoursCount(int hoursCount) {
        this.hoursCount = hoursCount;
    }
}
