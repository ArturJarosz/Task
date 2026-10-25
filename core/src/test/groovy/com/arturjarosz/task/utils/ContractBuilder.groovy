package com.arturjarosz.task.utils

import com.arturjarosz.task.contract.model.Contract
import com.arturjarosz.task.contract.status.ContractStatus
import com.arturjarosz.task.sharedkernel.model.Money
import com.arturjarosz.task.sharedkernel.testhelpers.AbstractBuilder
import com.arturjarosz.task.sharedkernel.testhelpers.TestUtils

import java.time.LocalDate

class ContractBuilder extends AbstractBuilder<Contract, ContractBuilder> {
    private static final String DEADLINE = "deadline"
    private static final String ID = "id"
    private static final String OFFER_VALUE = "offerValue"
    private static final String START_DATE = "startDate"
    private static final String STATUS = "status"

    ContractBuilder() {
        super(Contract)
    }

    ContractBuilder withId(Long id) {
        TestUtils.setFieldForObject(this.object, ID, id)
        return this
    }

    ContractBuilder withStatus(ContractStatus status) {
        TestUtils.setFieldForObject(this.object, STATUS, status)
        return this
    }

    ContractBuilder withDeadline(LocalDate deadline) {
        TestUtils.setFieldForObject(this.object, DEADLINE, deadline)
        return this
    }

    ContractBuilder withStartDate(LocalDate startDate) {
        TestUtils.setFieldForObject(this.object, START_DATE, startDate)
        return this
    }

    ContractBuilder withContractValue(Money money) {
        TestUtils.setFieldForObject(this.object, OFFER_VALUE, money)
        return this
    }
}
