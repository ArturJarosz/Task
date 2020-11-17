package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import javax.persistence.Column;

public class StageType extends AbstractEntity {

    private static final long serialVersionUID = 1925186033334202214L;
    @Column(name = "NAME")
    private String name;

    protected StageType() {
        //needed by Hibernate
    }

}
