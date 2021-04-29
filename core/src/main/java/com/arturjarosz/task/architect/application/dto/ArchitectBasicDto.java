package com.arturjarosz.task.architect.application.dto;

import java.io.Serializable;

public class ArchitectBasicDto implements Serializable {

    private static final long serialVersionUID = -3213904554343745120L;
    private Long id;
    private String firstName;
    private String lastName;

    public ArchitectBasicDto() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
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
}
