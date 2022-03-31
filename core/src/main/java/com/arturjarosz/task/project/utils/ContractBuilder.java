package com.arturjarosz.task.project.utils;

import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.sharedkernel.utils.AbstractBuilder;
import com.arturjarosz.task.sharedkernel.utils.TestUtils;

public class ContractBuilder extends AbstractBuilder<Contract, ContractBuilder> {
    private static final String ID = "id";

    public ContractBuilder() {
        super(Contract.class);
    }

    public ContractBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id);
        return this;
    }
}
