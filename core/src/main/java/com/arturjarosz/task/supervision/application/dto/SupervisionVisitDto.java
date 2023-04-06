package com.arturjarosz.task.supervision.application.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDate;

@Data
@NoArgsConstructor
public class SupervisionVisitDto implements Serializable {
    @JsonIgnore
    public static final String ID = "id";
    @JsonIgnore
    public static final String DATE_OF_VISIT = "dateOfVisit";
    @JsonIgnore
    public static final String PAYABLE = "payable";
    @JsonIgnore
    public static final String HOURS_COUNT = "hoursCount";
    @Serial
    private static final long serialVersionUID = 3217650597832433311L;
    private long supervisionId;
    private long id;
    private LocalDate dateOfVisit;
    private Boolean payable;
    private Integer hoursCount;

}
