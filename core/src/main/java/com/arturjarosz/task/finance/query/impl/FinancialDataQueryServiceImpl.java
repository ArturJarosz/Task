package com.arturjarosz.task.finance.query.impl;

import com.arturjarosz.task.dto.ContractorJobDto;
import com.arturjarosz.task.dto.CostDto;
import com.arturjarosz.task.dto.InstallmentDto;
import com.arturjarosz.task.dto.SupplyDto;
import com.arturjarosz.task.dto.TotalProjectFinancialSummaryDto;
import com.arturjarosz.task.finance.application.mapper.ContractorJobFields;
import com.arturjarosz.task.finance.application.mapper.CostFields;
import com.arturjarosz.task.finance.application.mapper.InstallmentFields;
import com.arturjarosz.task.finance.application.mapper.SupplyFields;
import com.arturjarosz.task.finance.domain.dto.FinancialDataDto;
import com.arturjarosz.task.finance.model.PartialFinancialDataType;
import com.arturjarosz.task.finance.model.QContractorJob;
import com.arturjarosz.task.finance.model.QCost;
import com.arturjarosz.task.finance.model.QFinancialData;
import com.arturjarosz.task.finance.model.QInstallment;
import com.arturjarosz.task.finance.model.QProjectFinancialData;
import com.arturjarosz.task.finance.model.QProjectFinancialPartialSummary;
import com.arturjarosz.task.finance.model.QProjectFinancialSummary;
import com.arturjarosz.task.finance.model.QSupply;
import com.arturjarosz.task.finance.model.dto.SupervisionRatesDto;
import com.arturjarosz.task.finance.model.dto.SupervisionVisitFinancialDto;
import com.arturjarosz.task.finance.query.FinancialDataQueryService;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supervision.model.QSupervision;
import com.arturjarosz.task.supervision.model.QSupervisionVisit;
import com.querydsl.core.types.ConstructorExpression;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.QBean;
import com.querydsl.jpa.impl.JPAQuery;

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
    private static final QProjectFinancialSummary PROJECT_FINANCIAL_SUMMARY = QProjectFinancialSummary.projectFinancialSummary;
    private static final QProjectFinancialPartialSummary PROJECT_FINANCIAL_PARTIAL_SUMMARY = QProjectFinancialPartialSummary.projectFinancialPartialSummary;
    private static final QProjectFinancialData PROJECT_FINANCIAL_DATA = QProjectFinancialData.projectFinancialData;

    public FinancialDataQueryServiceImpl() {
        super(FINANCIAL_DATA);
    }

    private static ConstructorExpression<TotalProjectFinancialSummaryDto> projectQueryToTotalProjectFinancialSummaryDto() {
        return Projections.constructor(TotalProjectFinancialSummaryDto.class,
                PROJECT_FINANCIAL_PARTIAL_SUMMARY.grossValue.value.doubleValue(),
                PROJECT_FINANCIAL_PARTIAL_SUMMARY.netValue.value.doubleValue(),
                PROJECT_FINANCIAL_PARTIAL_SUMMARY.vatTax.value.doubleValue(),
                PROJECT_FINANCIAL_PARTIAL_SUMMARY.incomeTax.value.doubleValue());
    }

    private static QBean<CostDto> costToCostDto() {
        return Projections.bean(CostDto.class, COST.id.as(CostFields.ID_FIELD),
                COST.financialData.value.value.as(CostFields.VALUE_FIELD), COST.name.as(CostFields.NAME_FIELD),
                COST.date.as(CostFields.DATE_FIELD), COST.note.as(CostFields.NOTE_FIELD),
                COST.category.as(CostFields.CATEGORY_FIELD), COST.financialData.hasInvoice.as(CostFields.HAS_INVOICE_FIELD),
                COST.financialData.paid.as(CostFields.IS_PAID_FIELD));
    }

    private static QBean<ContractorJobDto> contractorJobToContractorJobDto() {
        return Projections.bean(ContractorJobDto.class, CONTRACTOR_JOB.id.as(ContractorJobFields.ID_FIELD),
                CONTRACTOR_JOB.name.as(ContractorJobFields.NAME_FILED),
                CONTRACTOR_JOB.note.as(ContractorJobFields.NOTE_FILED),
                CONTRACTOR_JOB.financialData.paid.as(ContractorJobFields.PAID_FIELD),
                CONTRACTOR_JOB.financialData.hasInvoice.as(ContractorJobFields.HAS_INVOICE_FILED),
                CONTRACTOR_JOB.financialData.value.value.as(ContractorJobFields.VALUE_FILED),
                CONTRACTOR_JOB.financialData.payable.as(ContractorJobFields.PAYABLE_FILED),
                CONTRACTOR_JOB.cooperatorId.as(ContractorJobFields.CONTRACTOR_ID_FILED));
    }

    private static QBean<SupplyDto> supplyToSupplyDto() {
        return Projections.bean(SupplyDto.class, SUPPLY.id.as(SupplyFields.ID_FIELD), SUPPLY.name.as(SupplyFields.NAME_FILED),
                SUPPLY.note.as(SupplyFields.NOTE_FILED), SUPPLY.financialData.paid.as(SupplyFields.PAID_FIELD),
                SUPPLY.financialData.hasInvoice.as(SupplyFields.HAS_INVOICE_FILED),
                SUPPLY.financialData.value.value.as(SupplyFields.VALUE_FILED),
                SUPPLY.financialData.payable.as(SupplyFields.PAYABLE_FILED),
                SUPPLY.cooperatorId.as(SupplyFields.SUPPLIER_ID_FILED));
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
    public List<SupervisionVisitFinancialDto> getVisitsFinancialDto(Long supervisionId) {
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
        JPAQuery<?> costsFinancialDataQuery = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.costs, COST)
                .join(COST.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId));
        return this.getFinancialDataForFinancialDataAwareObjects(costsFinancialDataQuery);
    }

    @Override
    public List<FinancialDataDto> getSuppliesFinancialData(long projectId) {
        JPAQuery<?> suppliesFinancialDataQuery = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.supplies, SUPPLY)
                .join(SUPPLY.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId));
        return this.getFinancialDataForFinancialDataAwareObjects(suppliesFinancialDataQuery);
    }

    @Override
    public List<FinancialDataDto> getContractorsJobsFinancialData(long projectId) {
        JPAQuery<?> contractorsJobsFinancialDataQuery = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.contractorJobs, CONTRACTOR_JOB)
                .join(CONTRACTOR_JOB.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId));
        return this.getFinancialDataForFinancialDataAwareObjects(contractorsJobsFinancialDataQuery);
    }

    @Override
    public FinancialDataDto getSupervisionFinancialData(long projectId) {
        return this.query()
                .from(SUPERVISION)
                .join(SUPERVISION.financialData, FINANCIAL_DATA)
                .where(SUPERVISION.projectId.eq(projectId))
                .select(Projections.bean(FinancialDataDto.class,
                        FINANCIAL_DATA.payable.as(FinancialDataDto.PAYABLE_FIELD),
                        FINANCIAL_DATA.hasInvoice.as(FinancialDataDto.HAS_INVOICE_FIELD),
                        FINANCIAL_DATA.paid.as(FinancialDataDto.PAID_FIELD),
                        FINANCIAL_DATA.value.value.as(FinancialDataDto.VALUE_FIELD)))
                .fetchOne();
    }

    @Override
    public List<FinancialDataDto> getInstallmentsFinancialData(long projectId) {
        JPAQuery<?> installmentsFinancialDataQuery = this.query()
                .from(PROJECT_FINANCIAL_DATA)
                .join(PROJECT_FINANCIAL_DATA.installments, INSTALLMENT)
                .join(INSTALLMENT.financialData, FINANCIAL_DATA)
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId));
        return this.getFinancialDataForFinancialDataAwareObjects(installmentsFinancialDataQuery);
    }

    @Override
    public TotalProjectFinancialSummaryDto getTotalProjectFinancialSummary(long projectId) {
        return this.query()
                .from(PROJECT_FINANCIAL_SUMMARY)
                .join(PROJECT_FINANCIAL_SUMMARY.partialSummaries, PROJECT_FINANCIAL_PARTIAL_SUMMARY)
                .where(PROJECT_FINANCIAL_SUMMARY.projectId.eq(projectId)
                        .and(PROJECT_FINANCIAL_PARTIAL_SUMMARY.dataType.eq(PartialFinancialDataType.TOTAL)))
                .select(projectQueryToTotalProjectFinancialSummaryDto())
                .fetchOne();
    }

    @Override
    public Boolean doesCostExistByCostId(long costId) {
        return this.query().from(COST).where(COST.id.eq(costId)).select(COST).fetchCount() > 0;
    }

    @Override
    public CostDto getCostById(long costId) {
        return this.query().from(COST).where(COST.id.eq(costId)).select(costToCostDto()).fetchOne();
    }

    @Override
    public List<CostDto> getCostsByProjectId(long projectId) {
        return this.query()
                .from(COST)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(COST.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(costToCostDto())
                .fetch();
    }

    @Override
    public List<InstallmentDto> getInstallmentsByProjectId(long projectId) {
        return this.query()
                .from(INSTALLMENT)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(INSTALLMENT.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(Projections.bean(InstallmentDto.class, INSTALLMENT.id.as(InstallmentFields.ID_FIELD),
                        INSTALLMENT.financialData.value.value.as(InstallmentFields.VALUE_FIELD),
                        INSTALLMENT.note.as(InstallmentFields.NOTE_FIELD),
                        INSTALLMENT.financialData.paid.as(InstallmentFields.IS_PAID_FIELD),
                        INSTALLMENT.financialData.hasInvoice.as(InstallmentFields.HAS_INVOICE_FIELD),
                        INSTALLMENT.financialData.paymentDate.as(InstallmentFields.PAYMENT_DATE)))
                .fetch();
    }

    @Override
    public boolean doesSupplyForProjectExists(long projectId, long supplyId) {
        return this.query()
                .from(SUPPLY)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(SUPPLY.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId).and(SUPPLY.id.eq(supplyId)))
                .fetchCount() > 0;
    }

    @Override
    public SupplyDto getSupplyById(Long supplyId) {
        return this.query().from(SUPPLY).where(SUPPLY.id.eq(supplyId)).select(supplyToSupplyDto()).fetchOne();
    }

    @Override
    public List<SupplyDto> getSuppliesForProject(Long projectId) {
        return this.query()
                .from(SUPPLY)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(SUPPLY.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(PROJECT_FINANCIAL_DATA.projectId.eq(projectId))
                .select(supplyToSupplyDto())
                .fetch();
    }

    @Override
    public ContractorJobDto getContractorJobById(long contractorJobId) {
        return this.query()
                .from(CONTRACTOR_JOB)
                .where(CONTRACTOR_JOB.id.eq(contractorJobId))
                .select(contractorJobToContractorJobDto())
                .fetchOne();
    }

    @Override
    public boolean doesInstallmentExistsByInstallmentId(long installmentId) {
        return this.query()
                .from(INSTALLMENT)
                .where(INSTALLMENT.id.eq(installmentId))
                .select(INSTALLMENT)
                .fetchCount() > 0;
    }

    private List<FinancialDataDto> getFinancialDataForFinancialDataAwareObjects(JPAQuery<?> jpaQuery) {
        return jpaQuery.select(
                Projections.bean(FinancialDataDto.class, FINANCIAL_DATA.payable.as(FinancialDataDto.PAYABLE_FIELD),
                        FINANCIAL_DATA.hasInvoice.as(FinancialDataDto.HAS_INVOICE_FIELD),
                        FINANCIAL_DATA.paid.as(FinancialDataDto.PAID_FIELD),
                        FINANCIAL_DATA.value.value.as(FinancialDataDto.VALUE_FIELD))).fetch();
    }

}
