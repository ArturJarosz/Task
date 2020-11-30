package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.model.Money;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@Table(name = "COST")
public class Cost extends AbstractEntity {

    private static final long serialVersionUID = 4833869293487851155L;
    @Column(name = "name", nullable = false)
    private String costName;

    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "COST_VALUE"))
    private Money costValue;

    @Column(name = "CATEGORY", nullable = false)
    @Enumerated(EnumType.STRING)
    private CostCategory costCategory;

    @Column(name = "DATE")
    private LocalDate costDate;

    @Column(name = "NOTE")
    private String note;

    protected Cost() {
        //needed by Hibernate
    }

    public Cost(String costName, Money costValue, CostCategory costCategory, LocalDate costDate) {
        this.costName = costName;
        this.costValue = costValue;
        this.costCategory = costCategory;
        this.costDate = costDate;
    }
}
