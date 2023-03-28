package com.arturjarosz.task.finance.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
public class SupplyDto implements Serializable {

    public static final String ID_FIELD = "id";
    public static final String NAME_FILED = "name";
    public static final String VALUE_FILED = "value";
    public static final String NOTE_FILED = "note";
    public static final String SUPPLIER_ID_FILED = "supplierId";
    public static final String HAS_INVOICE_FILED = "hasInvoice";
    public static final String PAYABLE_FILED = "payable";
    public static final String PAID_FIELD = "paid";

    private Long id;
    private String name;
    private BigDecimal value;
    private Long supplierId;
    private String note;
    private Boolean hasInvoice;
    private Boolean payable;
    private Long projectId;
    private Boolean paid;

}
