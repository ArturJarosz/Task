package com.arturjarosz.task.supervision.utils;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.Money;
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
    private static final String HOURS_COUNT = "hoursCount";
    private static final String VISIT_NET_RATE = "visitNetRate";
    private static final String BASE_NET_RATE = "baseNetRate";
    private static final String HOURLY_NET_RATE = "hourlyNetRate";

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

    public SupervisionBuilder withHoursCount(int hoursCount) {
        TestUtils.setFieldForObject(this.object, HOURS_COUNT, hoursCount);
        return this;
    }

    public SupervisionBuilder withVisitNetRate(Money visitNetRate) {
        TestUtils.setFieldForObject(this.object, VISIT_NET_RATE, visitNetRate);
        return this;
    }

    public SupervisionBuilder withBaseNetRate(Money baseNetRate) {
        TestUtils.setFieldForObject(this.object, BASE_NET_RATE, baseNetRate);
        return this;
    }

    public SupervisionBuilder withHourlyNetRate(Money hourlyNetRate){
        TestUtils.setFieldForObject(this.object, HOURLY_NET_RATE, hourlyNetRate);
        return this;
    }
}
