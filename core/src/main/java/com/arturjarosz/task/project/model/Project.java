package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

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

    @Getter
    @Setter
    @Column(name = "NAME", nullable = false)
    private String name;

    @Getter
    @Setter
    @Column(name = "ARCHITECT_ID", nullable = false)
    private Long architectId;

    @Getter
    @Column(name = "CLIENT_ID", nullable = false)
    private Long clientId;

    @Getter
    @Setter
    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Getter
    @Setter
    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Getter
    @Column(name = "NOTE")
    private String note;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Set<Stage> stages;

    @Getter
    @Setter
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

    public void updateProjectBasicData(String name, String note) {
        this.name = name;
        this.note = note;
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

    public long getContractId() {
        return this.contractId;
    }
}
