package com.arturjarosz.task.finance.infrastructure.impl;

import com.arturjarosz.task.finance.infrastructure.ProjectFinancialDataRepository;
import com.arturjarosz.task.finance.model.ProjectFinancialData;
import com.arturjarosz.task.finance.model.QProjectFinancialData;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class ProjectFinancialDataRepositoryImpl extends GenericJpaRepositoryImpl<ProjectFinancialData, QProjectFinancialData>
        implements ProjectFinancialDataRepository {

    public static final QProjectFinancialData PROJECT_FINANCIAL_DATA = QProjectFinancialData.projectFinancialData;

    public ProjectFinancialDataRepositoryImpl() {
        super(PROJECT_FINANCIAL_DATA);
    }
}
