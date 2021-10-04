package com.arturjarosz.task.supervision.utils;

import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;
import com.arturjarosz.task.supervision.model.SupervisionVisit;

import java.time.LocalDate;

public class SupervisionVisitBuilder extends AbstractBuilder<SupervisionVisit, SupervisionVisitBuilder> {
    private static final String ID = "id";
    private static final String DATE_OF_VISIT = "dateOfVisit";
    private static final String HOURS_COUNT = "hoursCount";
    private static final String IS_PAYABLE = "isPayable";

    public SupervisionVisitBuilder(){
        super(SupervisionVisit.class);
    }

    public SupervisionVisitBuilder withId(Long id){
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public SupervisionVisitBuilder withDateOfVisit(LocalDate dateOfVisit){
        TestUtils.setFieldForObject(this.object, DATE_OF_VISIT, dateOfVisit);
        return this;
    }

    public SupervisionVisitBuilder withHoursCount(int hoursCount){
        TestUtils.setFieldForObject(this.object, HOURS_COUNT, hoursCount);
        return this;
    }

    public SupervisionVisitBuilder withIsPayable(boolean isPayable){
        TestUtils.setFieldForObject(this.object, IS_PAYABLE, isPayable);
        return this;
    }
}
