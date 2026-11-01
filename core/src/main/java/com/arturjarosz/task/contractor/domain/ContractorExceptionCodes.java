package com.arturjarosz.task.contractor.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public final class ContractorExceptionCodes {
    public static final String CONTRACTOR = "contractor";
    public static final String CONTRACTOR_CATEGORY = "contractorCategory";

    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String CONTRACTORS = "contractors";
    public static final String TOO_LONG = "tooLong";
    public static final String UNIQUE = "unique";

    private ContractorExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
