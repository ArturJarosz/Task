package com.arturjarosz.task.finance.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SupervisionRatesDto {
    public static final String BASE_NET_RATE = "baseNetRate";
    public static final String HOURLY_NET_RATE = "hourlyNetRate";
    public static final String VISIT_NET_RATE = "visitNetRate";

    private BigDecimal baseNetRate;
    private BigDecimal hourlyNetRate;
    private BigDecimal visitNetRate;

}
