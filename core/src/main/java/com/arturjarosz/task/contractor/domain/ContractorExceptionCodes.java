package com.arturjarosz.task.contractor.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public final class ContractorExceptionCodes {
    public static final String CONTRACTOR = "contractor";

    public static final String NAME = "name";
    public static final String CATEGORY = "category";

    private ContractorExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
