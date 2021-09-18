package com.arturjarosz.task.project.application;

import com.arturjarosz.task.sharedkernel.exceptions.ExceptionCodes;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalStateException;

public class ProjectExceptionCodes {
    //Entities
    public static final String ARCHITECT = "architect";
    public static final String CLIENT = "client";
    public static final String CONTRACT = "contract";
    public static final String CONTRACTOR_JOB = "contractorJob";
    public static final String COST = "cost";
    public static final String INSTALLMENT = "installment";
    public static final String PROJECT = "project";
    public static final String STAGE = "stage";
    public static final String SUPERVISION = "supervision";
    public static final String TASK = "task";

    //Fields
    public static final String CATEGORY = "category";
    public static final String CONTRACTOR = "contractor";
    public static final String COST_DATE = "costDate";
    public static final String DEADLINE = "deadline";
    public static final String END_DATE = "endDate";
    public static final String HOURLY_NET_RATE = "hourlyNetRate";
    public static final String INVOICE_FLAG = "invoiceFlag";
    public static final String IS_PAID = "isPaid";
    public static final String NAME = "name";
    public static final String PAY_DATE = "payDate";
    public static final String SIGNING_DATE = "signingDate";
    public static final String START_DATE = "startDate";
    public static final String STATUS = "status";
    public static final String TYPE = "type";
    public static final String VALUE = "value";
    public static final String VISIT_NET_RATE = "visitNetRate";

    public static final String NEGATIVE = "negative";
    public static final String TRANSITION = "transition";

    public static final String CREATE = "create";
    public static final String CHANGE = "change";
    public static final String REOPEN = "reopen";
    public static final String SIGN = "sign";
    public static final String START_PROGRESS = "startProgress";
    public static final String UPDATE = "update";

    public static final String DONE = "done";
    public static final String REJECTED = "rejected";
    public static final String BASE_NET_RATE = "baseNetRate";

    private ProjectExceptionCodes() {
        throw new IllegalStateException(ExceptionCodes.NOT_FOR_INSTANTIATING);
    }
}
