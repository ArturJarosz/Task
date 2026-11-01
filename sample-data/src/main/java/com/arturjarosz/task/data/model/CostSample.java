package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostSample extends FinanceItemSample {
    // name of the Cost category
    private String category;
    private Integer dateDaysAgo;
}
