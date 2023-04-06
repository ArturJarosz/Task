package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.contract.status.ContractStatus;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

@Data
@NoArgsConstructor
public class ContractDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -7287890392206614919L;
    private Long id;
    private Double projectValue;
    private ContractStatus contractStatus;

}
