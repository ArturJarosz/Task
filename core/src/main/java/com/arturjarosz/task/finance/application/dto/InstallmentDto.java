package com.arturjarosz.task.finance.application.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class InstallmentDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -4338716799879536304L;

    public static final String ID_FIELD = "id";
    public static final String VALUE_FIELD = "value";
    public static final String NOTE_FIELD = "note";
    public static final String IS_PAID_FIELD = "paid";
    public static final String HAS_INVOICE_FIELD = "hasInvoice";
    public static final String PAYMENT_DATE = "paymentDate";

    private Long id;
    private BigDecimal value;
    private Boolean paid;
    private String note;
    private LocalDate paymentDate;
    private Boolean hasInvoice;
    private Long stageId;

}
