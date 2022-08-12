package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.ContractorJobDto;
import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;
import org.hibernate.annotations.Where;

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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_sequence", allocationSize = 1)
@Table(name = "PROJECT")
public class Project extends AbstractAggregateRoot implements WorkflowAware<ProjectStatus> {
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

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    private Set<Cost> costs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    @Where(clause = "TYPE = 'CONTRACTOR_JOB'")
    private Set<ContractorJob> contractorJobs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID", nullable = false)
    @Where(clause = "TYPE = 'SUPPLY'")
    private Set<Supply> supplies;

    @Column(name = "CONTRACT_ID", nullable = false)
    private long contractId;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    protected Project() {
        //needed by Hibernate test
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

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
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

    public void removeCost(Long costId) {
        this.costs.removeIf(cost -> cost.getId().equals(costId));
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

    public Stage updateStage(Long stageId, String stageName, String note,
                             StageType stageType, LocalDate deadline) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.update(stageName, note, stageType, deadline);
        return stageToUpdate;
    }

    public void addInstallmentToStage(Long stageId, Installment installment) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.setInstallment(installment);

    }

    public Cost updateCost(Long costId, String name, LocalDate date, BigDecimal value, CostCategory category,
                           String note) {
        Cost cost = this.getCosts().stream().filter(costOnProject -> costOnProject.getId().equals(costId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        cost.updateCost(name, value, date, note, category);
        return cost;
    }

    public void addTaskToStage(Long stageId, Task task) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.addTask(task);
    }

    public void removeTaskFromStage(Long stageId, Long taskId) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        stageToUpdate.removeTask(taskId);
    }

    public Task updateTaskOnStage(Long stageId, Long taskId, TaskInnerDto taskInnerDto) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return stageToUpdate.updateTask(taskId, taskInnerDto);
    }

    public void addContractorJob(ContractorJob contractorJob) {
        if (this.contractorJobs == null){
            this.contractorJobs = new HashSet<>();
        }
        this.contractorJobs.add(contractorJob);
    }

    public void addSupply(Supply supply) {
        if (this.supplies == null) {
            this.supplies = new HashSet<>();
        }
        this.supplies.add(supply);
    }

    public Set<ContractorJob> getContractorJobs() {
        return this.contractorJobs;
    }

    public void removeContractorJob(Long contractorJobId) {
        this.contractorJobs.removeIf(contractorJob -> contractorJob.getId().equals(contractorJobId));
    }

    public ContractorJob updateContractorJob(long contractorJobId, ContractorJobDto contractorJobDto) {
        ContractorJob contractorJob = this.contractorJobs.stream()
                .filter(contractorJobToUpdate -> contractorJobToUpdate.getId() == (contractorJobId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        contractorJob.update(contractorJobDto);
        return contractorJob;
    }

    public Supply updateSupply(long supplyId, SupplyDto supplyDto) {
        Supply supply = this.supplies.stream()
                .filter(supplyToUpdate -> supplyToUpdate.getId() == (supplyId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        supply.update(supplyDto);
        return supply;
    }

    public Set<Supply> getSupplies() {
        return this.supplies;
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

    public void removeSupply(Long supplyId) {
        this.supplies.removeIf(supply -> supply.getId().equals(supplyId));
    }
}
