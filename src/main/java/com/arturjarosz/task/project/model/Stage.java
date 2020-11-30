package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Entity
@Table(name = "STAGE")
public class Stage extends AbstractEntity {

    private static final long serialVersionUID = 3201266147496282083L;
    @Column(name = "NAME")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE_ID")
    private Set<Task> tasks;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE_ID")
    private Set<Installment> installments;

    @Fetch(FetchMode.JOIN)
    @ManyToOne(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE_TYPE_ID")
    private StageType stageType;

    public Stage() {

    }

    public Stage(String name, Set<Task> tasks, Set<Installment> installments,
                 StageType stageType) {
        this.name = name;
        this.tasks = tasks;
        this.installments = installments;
        this.stageType = stageType;
    }

    public String getName() {
        return this.name;
    }

    public Set<Task> getTasks() {
        return this.tasks;
    }

    public Set<Installment> getInstallments() {
        return this.installments;
    }

    public StageType getStageType() {
        return this.stageType;
    }

}
