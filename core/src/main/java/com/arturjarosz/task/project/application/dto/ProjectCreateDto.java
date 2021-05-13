package com.arturjarosz.task.project.application.dto;

import com.arturjarosz.task.project.model.ProjectType;

import java.io.Serializable;

public class ProjectCreateDto implements Serializable {
    private static final long serialVersionUID = -7596108006634813082L;

    private Long id;
    private String name;
    private Long architectId;
    private Long clientId;
    private ProjectType projectType;
    private Double offerValue;

    public ProjectCreateDto() {

    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getArchitectId() {
        return this.architectId;
    }

    public void setArchitectId(Long architectId) {
        this.architectId = architectId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public ProjectType getProjectType() {
        return this.projectType;
    }

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public Double getOfferValue() {
        return this.offerValue;
    }

    public void setOfferValue(Double offerValue) {
        this.offerValue = offerValue;
    }
}
