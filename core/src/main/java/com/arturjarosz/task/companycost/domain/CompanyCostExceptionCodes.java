package com.arturjarosz.task.companycost.domain;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;

public final class CompanyCostExceptionCodes {
    public static final String AMOUNT = "amount";
    public static final String CATEGORY = "category";
    public static final String CHARGE_DAY = "chargeDay";
    public static final String CHARGE_MONTH = "chargeMonth";
    public static final String COMPANY_COST = "companyCost";
    public static final String COMPANY_COST_CATEGORY = "companyCostCategory";
    public static final String COMPANY_COSTS = "companyCosts";
    public static final String DATE = "date";
    public static final String END_MONTH = "endMonth";
    public static final String FIRST = "first";
    public static final String KIND = "kind";
    public static final String NAME = "name";
    public static final String NET_VALUE = "netValue";
    public static final String NOTE = "note";
    public static final String ONE_TIME = "oneTime";
    public static final String PAID = "paid";
    public static final String PAID_CHARGES = "paidCharges";
    public static final String PAYMENT = "payment";
    public static final String PAYMENT_DATE = "paymentDate";
    public static final String PERIOD = "period";
    public static final String REPEATS = "repeats";
    public static final String START_DATE = "startDate";
    public static final String START_MONTH = "startMonth";
    public static final String TOO_LONG = "tooLong";
    public static final String UNIQUE = "unique";
    public static final String VALID_FROM = "validFrom";
    public static final String YEAR = "year";

    private CompanyCostExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
