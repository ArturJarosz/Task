package com.arturjarosz.task.project.model;

import com.arturjarosz.task.sharedkernel.model.AbstractEntity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "stage_sequence", allocationSize = 1)
@Table(name = "STAGE")
public class Stage extends AbstractEntity {

    private static final long serialVersionUID = 3201266147496282083L;
    @Column(name = "NAME")
    private String name;

/*    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE_ID")
    private Set<Task> tasks;*/

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    @Column(name = "NOTE")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "STAGE_TYPE")
    private StageType stageType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "INSTALLMENT_ID", referencedColumnName = "ID")
    private Installment installment;

    protected Stage() {
        //needed by Hibernate
    }

    public Stage(String name, StageType stageType) {
        this.name = name;
        this.stageType = stageType;
    }

    public void update(String name, String note, StageType stageType, LocalDate deadline) {
        this.name = name;
        this.note = note;
        this.stageType = stageType;
        this.deadline = deadline;
    }

    public Installment getInstallment() {
        return this.installment;
    }

    public void setInstallment(Installment installment) {
        this.installment = installment;
    }

    public void removeInstallment() {
        this.installment = null;
    }
}
