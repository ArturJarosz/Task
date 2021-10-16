package com.arturjarosz.task.finance.model.dto;

public class SupervisionVisitFinancialDto {
    public static final String PAYABLE = "payable";
    public static final String HOURS_COUNT = "hoursCount";

    private int hoursCount;
    private boolean payable;

    public SupervisionVisitFinancialDto() {
    }

    public int getHoursCount() {
        return hoursCount;
    }

    public void setHoursCount(int hoursCount) {
        this.hoursCount = hoursCount;
    }

    public boolean isPayable() {
        return payable;
    }

    public void setPayable(boolean payable) {
        this.payable = payable;
    }
}
