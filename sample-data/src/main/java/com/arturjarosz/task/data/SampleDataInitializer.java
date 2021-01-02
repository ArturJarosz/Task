package com.arturjarosz.task.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class SampleDataInitializer extends AbstractDataInitializer {

    private static final Logger LOG = LogManager.getLogger(SampleDataInitializer.class);

    private final ArchitectsInitializer architectsInitializer;
    private final ClientInitializer clientInitializer;

    public SampleDataInitializer(ArchitectsInitializer architectsInitializer, ClientInitializer clientInitializer) {
        this.architectsInitializer = architectsInitializer;
        this.clientInitializer = clientInitializer;
    }

    /**
     * Loading all sample data.
     */
    @Override
    @Transactional
    protected void loadData() {
        LOG.info("Loading sample data.");
        this.architectsInitializer.run();
        this.clientInitializer.run();
        LOG.info("All sample data loaded.");
    }
}
