package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;
import jakarta.persistence.*;

import java.io.Serial;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_sequence", allocationSize = 1)
@Table(name = "PROJECT")
public class Project extends AbstractAggregateRoot implements WorkflowAware<ProjectStatus> {
    @Serial
    private static final long serialVersionUID = 5437961881026141924L;

    @Column(name = "NAME", nullable = false)
    private String name;

    @Column(name = "ARCHITECT_ID", nullable = false)
    private Long architectId;

    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "NOTE")
    private String note;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Set<Stage> stages;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROJECT_TYPE")
    private ProjectType projectType;

    @Column(name = "CONTRACT_ID", nullable = false)
    private long contractId;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    protected Project() {
        // needed by JPA
    }

    public Project(String name, Long architectId, Long clientId, ProjectType projectType,
            ProjectWorkflow projectWorkflow, long contractId) {
        this.name = name;
        this.architectId = architectId;
        this.clientId = clientId;
        this.projectType = projectType;
        this.workflowName = projectWorkflow.getName();
        this.contractId = contractId;
    }

    public void finishProject(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void updateProjectData(String name, String note) {
        this.name = name;
        this.note = note;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
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

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public String getNote() {
        return this.note;
    }

    public void addStage(Stage stage) {
        if (this.stages == null) {
            this.stages = new HashSet<>();
        }
        this.stages.add(stage);
    }

    public Set<Stage> getStages() {
        if (this.stages != null) {
            return new HashSet<>(this.stages);
        }
        return new HashSet<>();
    }

    public void removeStage(Long stageId) {
        this.stages.removeIf(stage -> stageId.equals(stage.getId()));
    }

    public Stage updateStage(Long stageId, String stageName, String note, StageType stageType, LocalDate deadline) {
        Stage stageToUpdate = this.stages.stream()
                .filter(stage -> stage.getId().equals(stageId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.update(stageName, note, stageType, deadline);
        return stageToUpdate;
    }

    public void addTaskToStage(Long stageId, Task task) {
        Stage stageToUpdate = this.stages.stream()
                .filter(stage -> stage.getId().equals(stageId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.addTask(task);
    }

    public void removeTaskFromStage(Long stageId, Long taskId) {
        Stage stageToUpdate = this.stages.stream()
                .filter(stage -> stage.getId().equals(stageId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.removeTask(taskId);
    }

    public Task updateTaskOnStage(Long stageId, Long taskId, TaskInnerDto taskInnerDto) {
        Stage stageToUpdate = this.stages.stream()
                .filter(stage -> stage.getId().equals(stageId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return stageToUpdate.updateTask(taskId, taskInnerDto);
    }

    @Override
    public ProjectStatus getStatus() {
        return this.status;
    }

    @Override
    public String getWorkflowName() {
        return this.workflowName;
    }

    @Override
    public void changeStatus(ProjectStatus status) {
        this.status = status;
    }

}
