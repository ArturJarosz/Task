package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.stage.StageStatus;
import com.arturjarosz.task.project.status.stage.StageWorkflow;
import com.arturjarosz.task.sharedkernel.model.AbstractEntity;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;
import jakarta.persistence.*;

import java.io.Serial;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "stage_sequence", allocationSize = 1)
@Table(name = "STAGE")
public class Stage extends AbstractEntity implements WorkflowAware<StageStatus> {
    @Serial
    private static final long serialVersionUID = 3201266147496282083L;

    @Column(name = "NAME")
    private String name;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "STAGE_ID", nullable = false)
    private List<Task> tasks;

    @Column(name = "START_DATE")
    private LocalDate startDate;

    @Column(name = "END_DATE")
    private LocalDate endDate;

    @Column(name = "DEADLINE")
    private LocalDate deadline;

    @Column(name = "NOTE")
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "STAGE_TYPE", nullable = false)
    private StageType stageType;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private StageStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    protected Stage() {
        // needed by JPA
    }

    public Stage(String name, StageType stageType, StageWorkflow stageWorkflow) {
        this.name = name;
        this.stageType = stageType;
        this.workflowName = stageWorkflow.getName();
    }

    public void update(String name, String note, StageType stageType, LocalDate deadline) {
        this.name = name;
        this.note = note;
        this.stageType = stageType;
        this.deadline = deadline;
    }

    public void addTask(Task task) {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        this.tasks.add(task);
    }

    public List<Task> getTasks() {
        if (this.tasks == null) {
            this.tasks = new ArrayList<>();
        }
        return new ArrayList<>(this.tasks);
    }

    public void removeTask(Long taskId) {
        this.tasks.removeIf(task -> task.getId().equals(taskId));
    }

    public Task updateTask(Long taskId, TaskInnerDto taskInnerDto) {
        Task taskToUpdate = Objects.requireNonNull(
                this.tasks.stream().filter(task -> task.getId().equals(taskId)).findFirst().orElse(null));
        taskToUpdate.update(taskInnerDto);
        return taskToUpdate;
    }

    public String getName() {
        return this.name;
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

    public StageType getStageType() {
        return this.stageType;
    }

    @Override
    public StageStatus getStatus() {
        return this.status;
    }

    public void setStatus(StageStatus status) {
        this.status = status;
    }

    @Override
    public String getWorkflowName() {
        return this.workflowName;
    }

    @Override
    public void changeStatus(StageStatus status) {
        this.status = status;
    }
}
