package com.arturjarosz.task.project.model;

import com.arturjarosz.task.project.application.dto.SupplyDto;
import com.arturjarosz.task.project.model.dto.TaskInnerDto;
import com.arturjarosz.task.project.status.project.ProjectStatus;
import com.arturjarosz.task.project.status.project.ProjectWorkflow;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import com.arturjarosz.task.sharedkernel.status.WorkflowAware;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
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
    @JoinColumn(name = "PROJECT_ID")
    private Set<Stage> stages;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROJECT_TYPE")
    private ProjectType projectType;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID")
    private Set<Cost> costs;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_ID")
    private Set<CooperatorJob> cooperatorJobs;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "ARRANGEMENT_ID")
    private Arrangement arrangement;

    @Column(name = "STATUS", nullable = false)
    @Enumerated(value = EnumType.STRING)
    private ProjectStatus status;

    @Column(name = "WORKFLOW_NAME", nullable = false)
    private String workflowName;

    protected Project() {
        //needed by Hibernate
    }

    public Project(String name, Long architectId, Long clientId, ProjectType projectType,
                   ProjectWorkflow projectWorkflow) {
        this.name = name;
        this.architectId = architectId;
        this.clientId = clientId;
        this.projectType = projectType;
        this.workflowName = projectWorkflow.getName();
        this.arrangement = new Offer(0);
    }

    public void signContract(LocalDate signingDate, LocalDate startDate, LocalDate deadline) {
        this.updateProjectDates(startDate);
        this.arrangement = new Contract(this.arrangement.getOfferValue().getValue().doubleValue(), signingDate,
                deadline);
        ((Contract) this.arrangement).setSigningDate(signingDate);
    }

    public void updateProjectDates(LocalDate startDate) {
        this.startDate = startDate;
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

    public LocalDate getSigningDate() {
        if (this.arrangement instanceof Contract) {
            return ((Contract) this.arrangement).getSigningDate();
        }
        return null;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public LocalDate getDeadline() {
        if (this.arrangement instanceof Contract) {
            return ((Contract) this.arrangement).getDeadline();
        }
        return null;
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
        return new HashSet<>(this.stages);
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

    public Task updateTaskOnStage(Long stageId, Long taskId,
                                  TaskInnerDto taskInnerDto) {
        Stage stageToUpdate = this.stages.stream().filter(stage -> stage.getId().equals(stageId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        return stageToUpdate.updateTask(taskId, taskInnerDto);
    }

    public void addCooperatorJob(CooperatorJob cooperatorJob) {
        if (this.cooperatorJobs == null) {
            this.cooperatorJobs = new HashSet<>();
        }
        this.cooperatorJobs.add(cooperatorJob);
    }

    public void addSupply(Supply supply) {
        if (this.cooperatorJobs == null) {
            this.cooperatorJobs = new HashSet<>();
        }
        this.cooperatorJobs.add(supply);
    }

    public Set<CooperatorJob> getCooperatorJobs() {
        return new HashSet<>(this.cooperatorJobs);
    }

    public void removeContractorJob(Long contractorJobId) {
        this.cooperatorJobs.removeIf(cooperatorJob -> cooperatorJob.getId().equals(contractorJobId));
    }

    public CooperatorJob updateContractorJob(Long contractorJobId, String name, BigDecimal value, String note) {
        CooperatorJob cooperatorJob = this.cooperatorJobs.stream()
                .filter(cooperatorJobOnProject -> cooperatorJobOnProject.getId().equals(contractorJobId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        cooperatorJob.setName(name);
        cooperatorJob.setValue(value);
        cooperatorJob.setNote(note);
        return cooperatorJob;
    }

    public Supply updateSupply(Long supplyId, SupplyDto supplyDto) {
        CooperatorJob cooperatorJob = this.cooperatorJobs.stream()
                .filter(cooperatorJobToUpdate -> cooperatorJobToUpdate.getCooperatorId().equals(supplyId)).findFirst()
                .orElseThrow(IllegalArgumentException::new);
        cooperatorJob.setName(supplyDto.getName());
        cooperatorJob.setValue(supplyDto.getValue());
        cooperatorJob.setNote(supplyDto.getNote());
        return (Supply) cooperatorJob;
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

    public void makeNewOffer(double offerValue) {
        this.arrangement = new Offer(offerValue);
    }

    public void acceptOffer() {
        ((Offer) this.arrangement).acceptOffer();
    }

    public Offer getOffer() {
        return ((Offer) this.arrangement);
    }

    public boolean isContractSigned() {
        return (this.arrangement instanceof Contract);
    }

    public boolean isOfferAccepted() {
        if (this.arrangement == null) {
            return false;
        }
        if (this.arrangement instanceof Offer) {
            return ((Offer) this.arrangement).isAccepted();
        }
        return true;
    }

    public Arrangement getArrangement() {
        return this.arrangement;
    }

    public void removeSupply(Long supplyId) {
        this.cooperatorJobs.removeIf(supply -> supply.getId().equals(supplyId));
    }
}
