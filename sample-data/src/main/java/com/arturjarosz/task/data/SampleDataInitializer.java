package com.arturjarosz.task.data;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import com.arturjarosz.task.sharedkernel.exceptions.SampleDataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@ApplicationService
public class SampleDataInitializer extends AbstractDataInitializer {

    private static final Logger LOG = LoggerFactory.getLogger(SampleDataInitializer.class);
    private final List<DataInitializer> dataInitializers;
    private final TransactionHandler transactionHandler;

    @Autowired
    public SampleDataInitializer(List<DataInitializer> dataInitializers, TransactionHandler transactionHandler) {
        this.dataInitializers = dataInitializers;
        this.transactionHandler = transactionHandler;
    }

    /**
     * Loading all sample data.
     */

    @Override
    protected void loadData() {
        LOG.info("Loading sample data.");

        for (DataInitializer dataInitializer : this.dataInitializers) {
            try {
                this.transactionHandler.runInTransaction(transactionStatus -> {
                    dataInitializer.initializeData();
                    return null;
                });
            } catch (Exception e) {
                throw new SampleDataException("Cannot load sample data.", e);
            }
        }

        LOG.info("All sample data loaded.");
    }
}
