package com.arturjarosz.task.data.model;

import com.arturjarosz.task.dto.ContractStatusDto;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * Sample Project definition. Related objects are referenced by natural keys, and dates are given as day offsets
 * from the day of loading the data, so the data stays valid for validators checking dates against today.
 * Creation time of the Project and its objects is moved back by {@code createdDaysAgo} days. Dates that can't be set
 * through application services (passed deadlines, start and end dates of work) are set directly in the database.
 */
@Getter
@Setter
public class ProjectSample {
    private String name;
    // name of the Project type
    private String type;
    private String clientEmail;
    private String architect;
    private Double offerValue;
    private Integer deadlineInDays;
    private ContractStatusDto contractStatus;
    private Integer signedDaysAgo;
    // end date of terminated or completed contract, today when not provided
    private Integer contractEndedDaysAgo;
    private int createdDaysAgo;
    private boolean rejected;
    private List<StageSample> stages = new ArrayList<>();
    private List<CostSample> costs = new ArrayList<>();
    private List<ContractorJobSample> contractorJobs = new ArrayList<>();
    private List<SupplySample> supplies = new ArrayList<>();
}
