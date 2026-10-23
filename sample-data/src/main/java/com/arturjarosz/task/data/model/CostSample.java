package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.CostCategoryDto;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CostSample extends FinanceItemSample {
    private CostCategoryDto category;
    private Integer dateDaysAgo;
}
