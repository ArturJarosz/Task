package com.arturjarosz.task.supervision.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.Serializable;
import java.math.BigDecimal;

public class SupervisionDto implements Serializable {
    private static final long serialVersionUID = 6120237413436097370L;

    @JsonIgnore
    public String ID_NAME = "id";
    @JsonIgnore
    public String HAS_INVOICE = "hasInvoice";
    @JsonIgnore
    public String BASE_NET_RATE = "baseNetRate";
    @JsonIgnore
    public String HOURLY_NET_RATE = "hourlyNetRate";
    @JsonIgnore
    public String VISIT_NET_RATE = "visitNetRate";

    private Long id;
    private Long projectId;
    private Boolean hasInvoice;
    private BigDecimal baseNetRate;
    private BigDecimal hourlyNetRate;
    private BigDecimal visitNetRate;
    private int hoursCount;
    private BigDecimal value;
    private String note;

    public SupervisionDto() {
        //needed by Hibernate
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Boolean getHasInvoice() {
        return this.hasInvoice;
    }

    public void setHasInvoice(Boolean hasInvoice) {
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

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public BigDecimal getValue() {
        return this.value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }
}
