package com.arturjarosz.task.supervision.utils;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;
import com.arturjarosz.task.supervision.model.Supervision;
import com.arturjarosz.task.supervision.model.SupervisionVisit;

import java.util.HashSet;
import java.util.Set;

public class SupervisionBuilder extends AbstractBuilder<Supervision, SupervisionBuilder> {

    private static final String FINANCIAL_DATA = "financialData";
    private static final String ID = "id";
    private static final String SUPERVISION_VISITS = "supervisionVisits";

    public SupervisionBuilder() {
        super(Supervision.class);
    }

    public SupervisionBuilder withFinancialData(FinancialData financialData) {
        TestUtils.setFieldForObject(this.object, FINANCIAL_DATA, financialData);
        return this;
    }

    public SupervisionBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public SupervisionBuilder withSupervisionVisit(SupervisionVisit supervisionVisit) {
        Set<SupervisionVisit> visits = new HashSet<>();
        visits.add(supervisionVisit);
        TestUtils.setFieldForObject(this.object, SUPERVISION_VISITS, visits);
        return this;
    }
}
