package com.arturjarosz.task.finance.query.impl;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.application.mapper.ContractorJobMapper;
import com.arturjarosz.task.finance.application.mapper.CostMapper;
import com.arturjarosz.task.finance.application.mapper.FinancialDataMapper;
import com.arturjarosz.task.finance.application.mapper.InstallmentMapper;
import com.arturjarosz.task.finance.application.mapper.ProjectFinancialPartialDataMapper;
import com.arturjarosz.task.finance.application.mapper.SupplyMapper;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.model.ProjectFinancialPartialData;
import com.arturjarosz.task.finance.model.QContractorJob;
import com.arturjarosz.task.finance.model.QCost;
import com.arturjarosz.task.finance.model.QFinancialData;
import com.arturjarosz.task.finance.model.QInstallment;
import com.arturjarosz.task.finance.model.QProjectFinancialData;
import com.arturjarosz.task.finance.model.QProjectFinancialPartialData;
import com.arturjarosz.task.finance.model.QSupply;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supervision.model.QSupervision;
import com.arturjarosz.task.supervision.model.QSupervisionVisit;
import com.querydsl.core.types.Projections;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Finder
public class FinancialDataQueryServiceImpl extends AbstractQueryService<QFinancialData> implements FinancialDataQueryService {
    private static final QContractorJob CONTRACTOR_JOB = QContractorJob.contractorJob;
    private static final QCost COST = QCost.cost;
    private static final QFinancialData FINANCIAL_DATA = QFinancialData.financialData;
    private static final QInstallment INSTALLMENT = QInstallment.installment;
    private static final QSupervision SUPERVISION = QSupervision.supervision;
    private static final QSupervisionVisit SUPERVISION_VISIT = QSupervisionVisit.supervisionVisit;
    private static final QSupply SUPPLY = QSupply.supply;
    private static final QProjectFinancialPartialData PROJECT_FINANCIAL_PARTIAL_DATA = QProjectFinancialPartialData.projectFinancialPartialData;
    private static final QProjectFinancialData PROJECT_FINANCIAL_DATA = QProjectFinancialData.projectFinancialData;

    private final FinancialDataMapper financialDataMapper;
    private final ProjectFinancialPartialDataMapper projectFinancialPartialDataMapper;
    private final CostMapper costMapper;
    private final InstallmentMapper installmentMapper;
    private final SupplyMapper supplyMapper;
    private final ContractorJobMapper contractorJobMapper;

    @Autowired
    public FinancialDataQueryServiceImpl(FinancialDataMapper financialDataMapper,
            ProjectFinancialPartialDataMapper projectFinancialPartialDataMapper, CostMapper costMapper,
            InstallmentMapper installmentMapper, SupplyMapper supplyMapper, ContractorJobMapper contractorJobMapper) {
        super(FINANCIAL_DATA);
        this.financialDataMapper = financialDataMapper;
        this.projectFinancialPartialDataMapper = projectFinancialPartialDataMapper;
        this.costMapper = costMapper;
        this.installmentMapper = installmentMapper;
        this.supplyMapper = supplyMapper;
        this.contractorJobMapper = contractorJobMapper;
    }

    @Override
    public SupervisionRatesDto getSupervisionRatesDto(long supervisionId) {
        return this.query()
                .from(SUPERVISION)
                .where(SUPERVISION.id.eq(supervisionId))
                .select(Projections.bean(SupervisionRatesDto.class,
                        SUPERVISION.baseNetRate.value.as(SupervisionRatesDto.BASE_NET_RATE),
                        SUPERVISION.hourlyNetRate.value.as(SupervisionRatesDto.HOURLY_NET_RATE),
                        SUPERVISION.visitNetRate.value.as(SupervisionRatesDto.VISIT_NET_RATE)))
                .fetchOne();
    }

    @Override
    public List<SupervisionVisitFinancialDto> getVisitsFinancialDto(long supervisionId) {
        return this.query()
                .from(SUPERVISION)
                .where(SUPERVISION.id.eq(supervisionId))
                .join(SUPERVISION.supervisionVisits, SUPERVISION_VISIT)
                .select(Projections.bean(SupervisionVisitFinancialDto.class,
                        SUPERVISION_VISIT.payable.as(SupervisionVisitFinancialDto.PAYABLE_FIELD),
                        SUPERVISION_VISIT.hoursCount.as(SupervisionVisitFinancialDto.HOURS_COUNT_FIELD)))
                .fetch();
    }

    @Override
    public List<FinancialDataDto> getCostsFinancialData(long projectId) {
        var costsFinancialData = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.costs, COST)
                .join(COST.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(FINANCIAL_DATA)
                .fetch();

        return costsFinancialData.stream().map(this.financialDataMapper::map).toList();
    }

    @Override
    public List<FinancialDataDto> getSuppliesFinancialData(long projectId) {
        var suppliesFinancialData = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.supplies, SUPPLY)
                .join(SUPPLY.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(FINANCIAL_DATA)
                .fetch();

        return suppliesFinancialData.stream().map(this.financialDataMapper::map).toList();
    }

    @Override
    public List<FinancialDataDto> getContractorsJobsFinancialData(long projectId) {
        var contractorsJobsFinancialData = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.contractorJobs, CONTRACTOR_JOB)
                .join(CONTRACTOR_JOB.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(FINANCIAL_DATA)
                .fetch();

        return contractorsJobsFinancialData.stream().map(this.financialDataMapper::map).toList();
    }

    @Override
    public FinancialDataDto getSupervisionFinancialData(long projectId) {
        var financialData = this.query()
                .from(SUPERVISION)
                .join(SUPERVISION.financialData, FINANCIAL_DATA)
                .where(SUPERVISION.projectId.eq(projectId))
                .select(FINANCIAL_DATA)
                .fetchOne();

        return this.financialDataMapper.map(financialData);
    }

    @Override
    public List<FinancialDataDto> getInstallmentsFinancialData(long projectId) {
        var installmentsFinancialData = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.installments, INSTALLMENT)
                .join(INSTALLMENT.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(FINANCIAL_DATA)
                .fetch();

        return installmentsFinancialData.stream().map(this.financialDataMapper::map).toList();
    }

    @Override
    public TotalProjectFinancialSummaryDto getTotalProjectFinancialSummary(long projectId) {
        var projectFinancialPartialSummary = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.partialSummaries, PROJECT_FINANCIAL_PARTIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId)
                        .and(PROJECT_FINANCIAL_PARTIAL_DATA.dataType.eq(PartialFinancialDataType.TOTAL)))
                .select(PROJECT_FINANCIAL_PARTIAL_DATA)
                .fetchOne();

        return this.projectFinancialPartialDataMapper.map(projectFinancialPartialSummary);
    }

    @Override
    public Boolean doesCostExistByCostId(long costId) {
        return !this.query().from(COST).where(COST.id.eq(costId)).select(COST.id).fetch().isEmpty();
    }

    @Override
    public CostDto getCostById(long costId) {
        var cost = this.query().from(COST).where(COST.id.eq(costId)).select(COST).fetchOne();

        return this.costMapper.mapToDto(cost);
    }

    @Override
    public List<CostDto> getCostsByProjectId(long projectId) {
        var costs = this.query()
                .from(COST)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(COST.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(COST)
                .fetch();

        return costs.stream().map(this.costMapper::mapToDto).toList();
    }

    @Override
    public List<InstallmentDto> getInstallmentsByProjectId(long projectId) {
        var installments = this.query()
                .from(INSTALLMENT)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(INSTALLMENT.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(INSTALLMENT)
                .fetch();

        return installments.stream().map(this.installmentMapper::mapToDto).toList();
    }

    @Override
    public boolean doesSupplyForProjectExists(long projectId, long supplyId) {
        return !this.query()
                .from(SUPPLY)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(SUPPLY.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId).and(SUPPLY.id.eq(supplyId)))
                .select(SUPPLY.id)
                .fetch()
                .isEmpty();
    }

    @Override
    public SupplyDto getSupplyById(long supplyId, long projectId) {
        var supply = this.query().from(SUPPLY).where(SUPPLY.id.eq(supplyId)).select(SUPPLY).fetchOne();

        return this.supplyMapper.mapToDto(supply, projectId);
    }

    @Override
    public List<SupplyDto> getSuppliesForProject(long projectId) {
        var supplies = this.query()
                .from(SUPPLY)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(SUPPLY.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(SUPPLY)
                .fetch();

        return supplies.stream().map(supply -> this.supplyMapper.mapToDto(supply, projectId)).toList();
    }

    @Override
    public ContractorJobDto getContractorJobById(long contractorJobId, long projectId) {
        var contractorJob = this.query()
                .from(CONTRACTOR_JOB)
                .where(CONTRACTOR_JOB.id.eq(contractorJobId))
                .select(CONTRACTOR_JOB)
                .fetchOne();
        if (contractorJob == null) {
            return null;
        }

        return this.contractorJobMapper.mapToDto(contractorJob, projectId);
    }

    @Override
    public boolean doesInstallmentExistsByInstallmentId(long installmentId) {
        return !this.query()
                .from(INSTALLMENT)
                .where(INSTALLMENT.id.eq(installmentId))
                .select(INSTALLMENT)
                .fetch()
                .isEmpty();
    }

    @Override
    public ProjectFinancialPartialData getInstallmentDataForProject(long projectId) {
        return this.query()
                .from(PROJECT_FINANCIAL_PARTIAL_DATA)
                .where(PROJECT_FINANCIAL_PARTIAL_DATA.projectFinancialDataId.eq(projectId))
                .where(PROJECT_FINANCIAL_PARTIAL_DATA.dataType.eq(PartialFinancialDataType.INSTALLMENT))
                .select(PROJECT_FINANCIAL_PARTIAL_DATA)
                .fetchOne();
    }

}
