package com.arturjarosz.task.systemparameter.domain.dto;

public class SystemParameterDto {

    private Long id;
    private String name;
    private String type;
    private String value;
    private String defaultValue;
    private Boolean singleValue;

    public SystemParameterDto() {
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

    public String getType() {
        return this.type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getDefaultValue() {
        return this.defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    public Boolean getSingleValue() {
        return this.singleValue;
    }

    public void setSingleValue(Boolean singleValue) {
        this.singleValue = singleValue;
    }
}
