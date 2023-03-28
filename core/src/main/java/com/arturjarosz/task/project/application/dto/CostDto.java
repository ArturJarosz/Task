package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.finance.model.CostCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class CostDto implements Serializable {

    @Serial
    private static final long serialVersionUID = 5692523801946817998L;

    public static final String ID_FIELD = "id";
    public static final String VALUE_FIELD = "value";
    public static final String NAME_FIELD = "name";
    public static final String CATEGORY_FIELD = "category";
    public static final String DATE_FIELD = "date";
    public static final String NOTE_FIELD = "note";
    public static final String HAS_INVOICE_FIELD = "hasInvoice";
    public static final String IS_PAID_FIELD = "paid";

    private Long id;
    private String name;
    private CostCategory category;
    private BigDecimal value;
    private LocalDate date;
    private String note;
    private Boolean hasInvoice;
    private Boolean payable;
    private Boolean paid;

}
