package com.arturjarosz.task.client.application.dto;

import java.io.Serializable;

public class ClientAdditionalDataDto implements Serializable {
    private static final long serialVersionUID = 3785221596347248231L;

    private String note;
    private Double projectValue;

    public ClientAdditionalDataDto() {
    }

    public String getNote() {
        return this.note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Double getProjectValue() {
        return this.projectValue;
    }

    public void setProjectValue(Double projectValue) {
        this.projectValue = projectValue;
    }
}
