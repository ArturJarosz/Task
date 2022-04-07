package com.arturjarosz.task.contract.intrastructure.impl;

import com.arturjarosz.task.contract.intrastructure.ContractRepository;
import com.arturjarosz.task.contract.model.Contract;
import com.arturjarosz.task.contract.model.QContract;
import com.arturjarosz.task.sharedkernel.infrastructure.impl.GenericJpaRepositoryImpl;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ContractRepositoryImpl extends GenericJpaRepositoryImpl<Contract, QContract> implements ContractRepository {
    public static final QContract CONTRACT = QContract.contract;

    public ContractRepositoryImpl() {
        super(CONTRACT);
    }
}
