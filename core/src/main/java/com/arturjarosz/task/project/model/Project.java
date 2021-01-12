package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.domain.ProjectDataValidator;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PROJECT")
public class Project extends AbstractAggregateRoot {
    private static final long serialVersionUID = 5437961881026141924L;

    @Transient
    private final ProjectDataValidator projectDataValidator = new ProjectDataValidator(this);

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
    private Set<Stage> stages;*/

    @Enumerated(EnumType.STRING)
    @Column(name = "PROJECT_TYPE")
    private ProjectType projectType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID")
    private Set<Cost> costs;

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
        this.projectDataValidator.signingDateNotInFuture(signingDate);
        //start date can't be before signing date
        this.projectDataValidator.startDateNotBeforeSigningDate(startDate, signingDate);
        this.signingDate = signingDate;
        //deadline can't be before start date
        this.projectDataValidator.deadlineNotBeforeStartDate(startDate, deadline);
        this.startDate = startDate;
        this.deadline = deadline;
    }

    public void finishProject(LocalDate endDate) {
        if (endDate == null) {
            endDate = LocalDate.now();
        }
        //end date can't be before start date
        this.projectDataValidator.endDateNotBeforeStartDate(this.startDate, endDate);
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

    public Long getArchitectId() {
        return this.architectId;
    }

    public Long getClientId() {
        return this.clientId;
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

    public void addCost(Cost cost) {
        if (this.costs == null) {
            this.costs = new HashSet<>();
        }
        this.costs.add(cost);
    }

    public Set<Cost> getCosts() {
        return new HashSet<>(this.costs);
    }
}
