package com.arturjarosz.task.supplier.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public class SupplierExceptionCodes {
    public static final String SUPPLIER = "supplier";

    public static final String NAME = "name";
    public static final String CATEGORY = "category";

    private SupplierExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
