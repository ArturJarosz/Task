package com.arturjarosz.task.finance.model;


import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.sharedkernel.exceptions.IllegalArgumentException;
import com.arturjarosz.task.sharedkernel.model.AbstractAggregateRoot;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import org.hibernate.annotations.Where;

import java.io.Serial;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings("java:S2160") // equality is tested on uuid value, no need to override with same code
@Entity
@Table(name = "PROJECT_FINANCIAL_DATA")
@SequenceGenerator(name = "sequence_generator", sequenceName = "project_financial_data_sequence", allocationSize = 1)
public class ProjectFinancialData extends AbstractAggregateRoot {

    @Serial
    private static final long serialVersionUID = -6717212464303174748L;
    @Getter
    @Column(name = "PROJECT_ID", nullable = false)
    private Long projectId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_FINANCIAL_DATA_ID", nullable = false)
    private Set<Cost> costs;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_FINANCIAL_DATA_ID", nullable = false)
    private Set<Installment> installments;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_FINANCIAL_DATA_ID", nullable = false)
    @Where(clause = "TYPE = 'CONTRACTOR_JOB'")
    private Set<ContractorJob> contractorJobs;

    @Getter
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "PROJECT_FINANCIAL_DATA_ID", nullable = false)
    @Where(clause = "TYPE = 'SUPPLY'")
    private Set<Supply> supplies;

    protected ProjectFinancialData() {
        // needed by JPA
    }

    public ProjectFinancialData(Long projectId) {
        this.projectId = projectId;
    }

    public void setProjectId(Long projectId) {
        this.projectId = projectId;
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

    public Cost updateCost(Long costId, String name, LocalDate date, BigDecimal value, CostCategory category,
            String note) {
        Cost cost = this.getCosts()
                .stream()
                .filter(costOnProject -> costOnProject.getId().equals(costId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        cost.updateCost(name, value, date, note, category);
        return cost;
    }

    public void addInstallment(Installment installment) {
        if (this.installments == null) {
            this.installments = new HashSet<>();
        }

        this.installments.add(installment);
    }

    public Installment getInstallment(Long installmentId) {
        return this.installments.stream()
                .filter(installment -> installment.getId().equals(installmentId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

    public void removeInstallment(Long installmentId) {
        this.installments.removeIf(installment -> installment.getId().equals(installmentId));
    }

    public void addContractorJob(ContractorJob contractorJob) {
        if (this.contractorJobs == null) {
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

    public void removeContractorJob(Long contractorJobId) {
        this.contractorJobs.removeIf(contractorJob -> contractorJob.getId().equals(contractorJobId));
    }

    public ContractorJob updateContractorJob(long contractorJobId, ContractorJobDto contractorJobDto) {
        ContractorJob contractorJob = this.contractorJobs.stream()
                .filter(contractorJobToUpdate -> contractorJobToUpdate.getId() == (contractorJobId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        contractorJob.update(contractorJobDto);
        return contractorJob;
    }

    public Supply updateSupply(long supplyId, SupplyDto supplyDto) {
        Supply supply = this.supplies.stream()
                .filter(supplyToUpdate -> supplyToUpdate.getId() == (supplyId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        supply.update(supplyDto);
        return supply;
    }

    public void removeSupply(Long supplyId) {
        this.supplies.removeIf(supply -> supply.getId().equals(supplyId));
    }

    public Installment updateInstallment(Long installmentId, InstallmentDto installmentDto) {
        Installment installment = this.installments.stream()
                .filter(installmentToUpdate -> installmentToUpdate.getId().equals(installmentId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        installment.update(installmentDto);
        return installment;
    }

    public Installment payInstallment(Long installmentId, LocalDate payDate) {
        Installment installment = this.installments.stream()
                .filter(installmentToUpdate -> installmentToUpdate.getId().equals(installmentId))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
        if (payDate == null) {
            payDate = LocalDate.now();
        }
        installment.payInstallment(payDate);
        return installment;
    }
}
