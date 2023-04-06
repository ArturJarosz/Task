package com.arturjarosz.task.finance.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class ContractorJobDto implements Serializable {
    public static final String ID_FIELD = "id";
    public static final String NAME_FILED = "name";
    public static final String VALUE_FILED = "value";
    public static final String NOTE_FILED = "note";
    public static final String CONTRACTOR_ID_FILED = "contractorId";
    public static final String HAS_INVOICE_FILED = "hasInvoice";
    public static final String PAYABLE_FILED = "payable";
    public static final String PAID_FIELD = "paid";
    @Serial
    private static final long serialVersionUID = -532775551414801250L;
    private Long id;
    private String name;
    private BigDecimal value;
    private Long contractorId;
    private String note;
    private Boolean hasInvoice;
    private Boolean payable;
    private Boolean paid;

}
