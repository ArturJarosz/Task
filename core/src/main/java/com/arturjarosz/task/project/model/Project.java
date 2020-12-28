package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.domain.ProjectValidator;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDate;

@Entity
@Table(name = "PROJECT")
public class Project extends AbstractAggregateRoot {
    private static final long serialVersionUID = 5437961881026141924L;

    @Transient
    private final ProjectValidator projectValidator = new ProjectValidator(this);

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ARCHITECT_ID", nullable = false)
    private Long architectId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Column(name = "SIGNING_DATE")
    private LocalDate signingDate;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    @Column(name = "NOTE")
    private String note;

/*    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID")
    private Set<Cost> costs;*/

/*    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID")
    private Set<Stage> stages;*/

    @Enumerated(EnumType.STRING)
    @Column(name = "PROJECT_TYPE")
    private ProjectType projectType;

    protected Project() {
        //needed by Hibernate
    }

    public Project(String name, Long architectId, Long clientId, ProjectType projectType) {
        this.name = name;
        this.architectId = architectId;
        this.clientId = clientId;
        this.projectType = projectType;
    }

    public void signContract(LocalDate signingDate, LocalDate startDate, LocalDate deadline) {
        this.updateProjectDates(signingDate, startDate, deadline);
    }

    public void updateProjectDates(LocalDate signingDate, LocalDate startDate, LocalDate deadline) {
        //signing date can't be future date
        this.projectValidator.signingDateNotInFuture(signingDate);
        //start date can't be before signing date
        this.projectValidator.startDateNotBeforeSigningDate(startDate, signingDate);
        this.signingDate = signingDate;
        //deadline can't be before start date
        this.projectValidator.deadlineNotBeforeStartDate(startDate, deadline);
        this.startDate = startDate;
        this.deadline = deadline;
    }

    public void finishProject(LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        //end date can't be before start date
        this.projectValidator.endDateNotBeforeStartDate(this.startDate, endDate);
        this.endDate = endDate;
    }

    public void updateProjectData(String name, String note) {
        //cannot
        this.name = name;
        this.note = note;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setArchitectId(Long architectId) {
        this.architectId = architectId;
    }

    public Long getArchitectId() {
        return this.architectId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }

    public Long getClientId() {
        return this.clientId;
    }

/*    public Set<Cost> getCosts() {
        return this.costs;
    }

    public Set<Stage> getStages() {
        return this.stages;
    }*/

    public void setProjectType(ProjectType projectType) {
        this.projectType = projectType;
    }

    public ProjectType getProjectType() {
        return this.projectType;
    }

    public LocalDate getSigningDate() {
        return this.signingDate;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public LocalDate getDeadline() {
        return this.deadline;
    }

    public String getNote() {
        return this.note;
    }
}
