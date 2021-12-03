package com.arturjarosz.task.data;

import com.arturjarosz.task.sharedkernel.annotations.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

@DomainService
public class TransactionHandler {
    private final PlatformTransactionManager transactionManager;

    @Autowired
    public TransactionHandler(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    public void runInTransaction(TransactionCallback<Void> transaction) {
        TransactionTemplate transactionTemplate = new TransactionTemplate(this.transactionManager);
        transactionTemplate.execute(transaction);
    }
}
