package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.CostCategory;

import java.time.LocalDate;

public class CostDomainService {

    public void updateCost(String name, Double value, CostCategory category, LocalDate date, String note,
                           Cost cost) {
        cost.updateCost(name, value, date, note, category);
    }
}
