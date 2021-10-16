package com.arturjarosz.task.supervision.utils;

import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.sharedkernel.model.Money;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;

public class FinancialDataBuilder extends AbstractBuilder<FinancialData, FinancialDataBuilder> {
    private static final String ID = "id";
    private static final String HAS_INVOICE = "hasInvoice";
    private static final String PAYABLE = "payable";
    private static final String VALUE = "value";

    public FinancialDataBuilder() {
        super(FinancialData.class);
    }

    public FinancialDataBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }

    public FinancialDataBuilder withHasInvoice(boolean hasInvoice) {
        TestUtils.setFieldForObject(this.object, HAS_INVOICE, hasInvoice);
        return this;
    }

    public FinancialDataBuilder withPayable(boolean payable) {
        TestUtils.setFieldForObject(this.object, PAYABLE, payable);
        return this;
    }

    public FinancialDataBuilder withValue(Money value) {
        TestUtils.setFieldForObject(this.object, VALUE, value);
        return this;
    }
}
