package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import javax.persistence.*;
import java.util.List;

public class Stage extends AbstractEntity {

    private static final long serialVersionUID = 3201266147496282083L;
    @Column(name = "NAME")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "STAGE_ID")
    private List<Task> tasks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "STAGE_ID")
    private List<Installment> installments;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE_TYPE_ID")
    private StageType stageType;
}
