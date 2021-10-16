package com.arturjarosz.task.supervision.domain.impl;

import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.supervision.domain.SupervisionCalculationService;
import com.arturjarosz.task.supervision.model.Supervision;
import com.arturjarosz.task.supervision.model.SupervisionVisit;

import java.math.BigDecimal;

@DomainService
public class SupervisionCalculationServiceImpl implements SupervisionCalculationService {

    @Override
    public void recalculateSupervision(Supervision supervision) {
        BigDecimal value = new BigDecimal(0);
        // Adding base rate
        value = value.add(BigDecimal.valueOf(supervision.getBaseNetRate().doubleValue()));
        if (supervision.getSupervisionVisits() != null) {
            // Adding hours value and rate per visit
            for (SupervisionVisit supervisionVisit : supervision.getSupervisionVisits()) {
                if (supervisionVisit.isPayable()) {
                    BigDecimal hoursValue = BigDecimal.valueOf(
                            supervisionVisit.getHoursCount() * supervision.getHourlyNetRate().doubleValue());
                    value = value.add(hoursValue);
                    value = value.add(supervision.getVisitNetRate());
                }
            }
        }
        supervision.getFinancialData().setValue(new Money(value));
    }
}
