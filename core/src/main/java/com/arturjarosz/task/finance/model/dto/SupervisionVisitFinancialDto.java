package com.arturjarosz.task.finance.model.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SupervisionVisitFinancialDto {
    public static final String PAYABLE_FIELD = "payable";
    public static final String HOURS_COUNT_FIELD = "hoursCount";

    private int hoursCount;
    private boolean payable;
}
