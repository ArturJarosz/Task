package com.arturjarosz.task.contract.application.dto;

import com.arturjarosz.task.contract.status.ContractStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class ContractDto implements Serializable {
    private static final long serialVersionUID = 2871016534140942045L;

    private Double offerValue;
    private LocalDate signingDate;
    private LocalDate deadline;
    private LocalDate startDate;
    private LocalDate endDate;
    private ContractStatus status;

}
