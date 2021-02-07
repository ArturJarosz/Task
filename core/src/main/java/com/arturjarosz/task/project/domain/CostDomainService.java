package com.arturjarosz.task.project.domain;

import com.arturjarosz.task.project.model.Cost;
import com.arturjarosz.task.project.model.CostCategory;

import java.time.LocalDate;

public class CostDomainService {

    public void updateCost(String name, Double value, CostCategory category, LocalDate date, String description,
                           Cost cost) {
        cost.updateCost(name, value, date, description, category);
    }
}
