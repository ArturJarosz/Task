package com.arturjarosz.task.supervision.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SupervisionDto implements Serializable {
    @Serial
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

}
