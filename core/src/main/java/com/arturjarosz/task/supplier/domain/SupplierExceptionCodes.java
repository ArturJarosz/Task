package com.arturjarosz.task.supplier.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public final class SupplierExceptionCodes {
    public static final String SUPPLIER = "supplier";
    public static final String SUPPLIER_CATEGORY = "supplierCategory";

    public static final String NAME = "name";
    public static final String CATEGORY = "category";
    public static final String SUPPLIERS = "suppliers";
    public static final String TOO_LONG = "tooLong";
    public static final String UNIQUE = "unique";

    private SupplierExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
