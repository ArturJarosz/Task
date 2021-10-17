package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.project.model.Installment;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;

public class InstallmentBuilder extends AbstractBuilder<Installment, InstallmentBuilder> {

    public static final String FINANCIAL_DATA = "financialData";
    public static final String NOTE = "note";

    public InstallmentBuilder() {
        super(Installment.class);
    }

    public InstallmentBuilder withNote(String note) {
        TestUtils.setFieldForObject(this.object, NOTE, note);
        return this;
    }

    public InstallmentBuilder withFinancialData(FinancialData financialData) {
        TestUtils.setFieldForObject(this.object, FINANCIAL_DATA, financialData);
        return this;
    }
}
