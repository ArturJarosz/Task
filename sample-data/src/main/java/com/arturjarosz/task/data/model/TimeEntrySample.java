package com.arturjarosz.task.data.model;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimeEntrySample {
    private int workedDaysAgo;
    private int minutes;
    private String note;
}
