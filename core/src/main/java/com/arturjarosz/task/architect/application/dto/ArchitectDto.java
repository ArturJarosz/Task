package com.arturjarosz.task.architect.application.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ArchitectDto implements Serializable {

    private static final long serialVersionUID = -2843189902148429731L;
    private Long id;
    private String firstName;
    private String lastName;
    private Double projectsValue;

}
