package com.arturjarosz.task.contractor.application.dto;

import com.arturjarosz.task.contractor.model.ContractorCategory;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class ContractorDto implements Serializable {
    private static final long serialVersionUID = 1761161251042111551L;

    private Long id;
    private String name;
    private String note;
    private String email;
    private String telephone;
    private Double jobsValue;
    private ContractorCategory category;

}
