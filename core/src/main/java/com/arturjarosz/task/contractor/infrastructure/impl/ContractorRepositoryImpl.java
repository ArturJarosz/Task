package com.arturjarosz.task.contractor.infrastructure.impl;

import com.arturjarosz.task.contractor.infrastructure.ContractorRepository;
import com.arturjarosz.task.contractor.model.Contractor;
import com.arturjarosz.task.contractor.model.QContractor;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;

@Repository
public class ContractorRepositoryImpl extends GenericJpaRepositoryImpl<Contractor, QContractor> implements ContractorRepository {
    private static final QContractor CONTRACTOR = QContractor.contractor;

    public ContractorRepositoryImpl() {
        super(CONTRACTOR);
    }
}
