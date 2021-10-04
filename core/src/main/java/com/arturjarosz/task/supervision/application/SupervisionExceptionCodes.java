package com.arturjarosz.task.supervision.application;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public class SupervisionExceptionCodes {

    // Entities
    public static final String SUPERVISION = "supervision";
    public static final String SUPERVISION_VISIT = "supervisionVisit";

    // Fields
    public static final String BASE_NET_RATE = "baseNetRate";
    public static final String DATE_OF_VISIT = "dateOfVisit";
    public static final String HOURS_COUNT = "hoursCount";
    public static final String HOURLY_NET_RATE = "hourlyNetRate";
    public static final String INVOICE_FLAG = "invoiceFlag";
    public static final String IS_PAYABLE_FLAG = "isPayableFlag";
    public static final String PROJECT_ID = "projectId";
    public static final String VISIT_NET_RATE = "visitNetRate";

    private SupervisionExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
