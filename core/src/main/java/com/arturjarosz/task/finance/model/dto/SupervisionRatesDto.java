package com.arturjarosz.task.finance.model.dto;

import java.math.BigDecimal;

public class SupervisionRatesDto {
    public static final String BASE_NET_RATE = "baseNetRate";
    public static final String HOURLY_NET_RATE = "hourlyNetRate";
    public static final String VISIT_NET_RATE = "visitNetRate";


    private BigDecimal baseNetRate;
    private BigDecimal hourlyNetRate;
    private BigDecimal visitNetRate;

    public SupervisionRatesDto() {
    }

    public BigDecimal getBaseNetRate() {
        return baseNetRate;
    }

    public void setBaseNetRate(BigDecimal baseNetRate) {
        this.baseNetRate = baseNetRate;
    }

    public BigDecimal getHourlyNetRate() {
        return hourlyNetRate;
    }

    public void setHourlyNetRate(BigDecimal hourlyNetRate) {
        this.hourlyNetRate = hourlyNetRate;
    }

    public BigDecimal getVisitNetRate() {
        return visitNetRate;
    }

    public void setVisitNetRate(BigDecimal visitNetRate) {
        this.visitNetRate = visitNetRate;
    }
}
