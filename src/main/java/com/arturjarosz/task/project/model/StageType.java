package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "STAGE_TYPE")
public class StageType extends AbstractEntity {

    private static final long serialVersionUID = 1925186033334202214L;
    @Column(name = "NAME")
    private String name;

    protected StageType() {
        //needed by Hibernate
    }

    public StageType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

}
