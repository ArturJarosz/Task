package com.arturjarosz.task.supplier.query.impl;

import com.arturjarosz.task.finance.application.mapper.FinancialDataMapper;
import com.arturjarosz.task.finance.application.mapper.SupplyMapper;
import com.arturjarosz.task.finance.model.QFinancialData;
import com.arturjarosz.task.finance.model.QProjectFinancialData;
import com.arturjarosz.task.finance.model.QSupply;
import com.arturjarosz.task.finance.model.SupplierSupplyDataDto;
import com.arturjarosz.task.sharedkernel.annotations.Finder;
import com.arturjarosz.task.sharedkernel.infrastructure.AbstractQueryService;
import com.arturjarosz.task.supplier.model.QSupplier;
import com.arturjarosz.task.supplier.query.SupplierQueryService;

import java.util.Set;
import java.util.stream.Collectors;

@Finder
public class SupplierQueryServiceImpl extends AbstractQueryService<QSupplier> implements SupplierQueryService {

    private static final QProjectFinancialData PROJECT_FINANCIAL_DATA = QProjectFinancialData.projectFinancialData;
    private static final QFinancialData FINANCIAL_DATA = QFinancialData.financialData;
    private static final QSupplier SUPPLIER = QSupplier.supplier;
    private static final QSupply SUPPLY = QSupply.supply;
    private final SupplyMapper supplyMapper;
    private final FinancialDataMapper financialDataMapper;

    public SupplierQueryServiceImpl(SupplyMapper supplyMapper, FinancialDataMapper financialDataMapper) {
        super(SUPPLIER);
        this.supplyMapper = supplyMapper;
        this.financialDataMapper = financialDataMapper;
    }

    @Override
    public boolean supplierWithIdExists(long supplierId) {
        return this.query().from(SUPPLIER).where(SUPPLIER.id.eq(supplierId)).fetchOne() != null;
    }

    @Override
    public Set<SupplierSupplyDataDto> getSupplierSuppliesData(long supplierId) {
        return this.query()
                .from(SUPPLY)
                .leftJoin(PROJECT_FINANCIAL_DATA)
                .on(SUPPLY.projectFinancialDataId.eq(PROJECT_FINANCIAL_DATA.id))
                .where(SUPPLY.supplierId.eq(supplierId))
                .select(SUPPLY, PROJECT_FINANCIAL_DATA.projectId)
                .fetch()
                .stream()
                .map(supplyAndProjectId -> new SupplierSupplyDataDto(
                        this.supplyMapper.mapToDto(supplyAndProjectId.get(SUPPLY),
                                supplyAndProjectId.get(PROJECT_FINANCIAL_DATA.projectId)),
                        this.financialDataMapper.map(supplyAndProjectId.get(SUPPLY).getFinancialData())))
                .collect(Collectors.toSet());
    }
}
