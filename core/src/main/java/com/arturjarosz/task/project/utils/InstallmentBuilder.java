package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;

import java.time.LocalDate;

public class InstallmentBuilder extends AbstractBuilder<Installment, InstallmentBuilder> {

    public static final String AMOUNT = "amount";
    public static final String DESCRIPTION = "description";
    public static final String IS_PAID = "isPaid";
    public static final String PAYMENT_DATE = "paymentDate";

    public InstallmentBuilder() {
        super(Installment.class);
    }

    public InstallmentBuilder withDescription(String description) {
        TestUtils.setFieldForObject(this.object, DESCRIPTION, description);
        return this;
    }

    public InstallmentBuilder withPayDate(LocalDate date) {
        TestUtils.setFieldForObject(this.object, PAYMENT_DATE, date);
        return this;
    }

    public InstallmentBuilder withIsPaid(Boolean isPaid) {
        TestUtils.setFieldForObject(this.object, IS_PAID, isPaid);
        return this;
    }

    public InstallmentBuilder withAmount(Money amount) {
        TestUtils.setFieldForObject(this.object, AMOUNT, amount);
        return this;
    }
}
