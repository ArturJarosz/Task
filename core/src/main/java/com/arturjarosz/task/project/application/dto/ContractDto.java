package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.contract.status.ContractStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ContractDto implements Serializable {
    private Long id;
    private Double projectValue;
    private ContractStatus contractStatus;

}
