package com.arturjarosz.task.finance.infrastructure.impl;

import com.arturjarosz.task.finance.infrastructure.FinancialDataRepository;
import com.arturjarosz.task.finance.model.FinancialData;
import com.arturjarosz.task.finance.model.QFinancialData;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class FinancialDataRepositoryImpl extends GenericJpaRepositoryImpl<FinancialData, QFinancialData>
        implements FinancialDataRepository {

    public static final QFinancialData FINANCIAL_DATA = QFinancialData.financialData;

    public FinancialDataRepositoryImpl() {
        super(FINANCIAL_DATA);
    }
}
