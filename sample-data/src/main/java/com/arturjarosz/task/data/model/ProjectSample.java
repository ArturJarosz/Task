package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.ContractStatusDto;
import com.arturjarosz.task.dto.ProjectTypeDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample Project definition. Related objects are referenced by natural keys, and dates are given as day offsets
 * from the day of loading the data, so the data stays valid for validators checking dates against today.
 */
@Getter
@Setter
public class ProjectSample {
    private String name;
    private ProjectTypeDto type;
    private String clientEmail;
    private String architect;
    private Double offerValue;
    private Integer deadlineInDays;
    private ContractStatusDto contractStatus;
    private Integer signedDaysAgo;
    private boolean rejected;
    private List<StageSample> stages = new ArrayList<>();
    private List<CostSample> costs = new ArrayList<>();
    private List<ContractorJobSample> contractorJobs = new ArrayList<>();
    private List<SupplySample> supplies = new ArrayList<>();
}
