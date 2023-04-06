package com.arturjarosz.task.systemparameter.domain.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SystemParameterDto {

    private Long id;
    private String name;
    private String type;
    private String value;
    private String defaultValue;
    private Boolean singleValue;

}
