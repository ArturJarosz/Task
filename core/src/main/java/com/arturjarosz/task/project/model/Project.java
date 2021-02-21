package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_sequence", allocationSize = 1)
@Table(name = "PROJECT")
public class Project extends AbstractAggregateRoot {
    private static final long serialVersionUID = 5437961881026141924L;

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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID")
    private List<Stage> stages;

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
        this.signingDate = signingDate;
        this.startDate = startDate;
        this.deadline = deadline;
    }

    public void finishProject(LocalDate endDate) {
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

    public void addStage(Stage stage) {
        if (this.stages == null) {
            this.stages = new ArrayList<>();
        }
        this.stages.add(stage);
    }

    public List<Stage> getStages() {
        return new ArrayList<>(this.stages);
    }

    public void removeStage(Long stageId) {
        this.stages.removeIf(stage -> stageId.equals(stage.getId()));
    }

    public void updateStage(Long stageId, String stageName, String note,
                            StageType stageType, LocalDate deadline) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow();
        stageToUpdate.update(stageName, note, stageType, deadline);
    }

    public void addInstallmentToStage(Long stageId, Installment installment) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow();
        stageToUpdate.setInstallment(installment);

    }

    public void updateCost(Long costId, String name, LocalDate date, Double value, CostCategory category,
                           String note) {
        Cost cost = this.getCosts().stream().filter(costOnProject -> costOnProject.getId().equals(costId)).findFirst()
                .orElseThrow();
        cost.updateCost(name, value, date, note, category);
    }

    public void addTaskToStage(Long stageId, Task task) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElse(null);
        stageToUpdate.addTask(task);
    }

    public void removeTaskFromStage(Long stageId, Long taskId) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElse(null);
        stageToUpdate.removeTask(taskId);
    }

    public void updateTaskOnStage(Long stageId, Long taskId,
                                  TaskInnerDto taskInnerDto) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElse(null);
        stageToUpdate.updateTask(taskId, taskInnerDto);
    }
}
