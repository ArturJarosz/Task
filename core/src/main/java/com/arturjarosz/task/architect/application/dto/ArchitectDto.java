package com.arturjarosz.task.architect.application.dto;

import java.io.Serializable;

public class ArchitectDto implements Serializable {

    private static final long serialVersionUID = -2843189902148429731L;
    private Long id;
    private String firstName;
    private String lastName;
    private Double projectsValue;

    public ArchitectDto() {
        //needed by Hibernate
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return this.lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Double getProjectsValue() {
        return this.projectsValue;
    }

    public void setProjectsValue(Double projectsValue) {
        this.projectsValue = projectsValue;
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }
}
