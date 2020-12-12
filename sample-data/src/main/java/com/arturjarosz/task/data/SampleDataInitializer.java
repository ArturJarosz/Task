package com.arturjarosz.task.data;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;

@Component
public class SampleDataInitializer extends AbstractDataInitializer {

    private static final Logger LOG = LogManager.getLogger(SampleDataInitializer.class);

    private final ArchitectsInitializer architectsInitializer;

    public SampleDataInitializer(ArchitectsInitializer architectsInitializer) {
        this.architectsInitializer = architectsInitializer;
    }

    @Override
    @Transactional
    protected void loadData() {
        LOG.info("Loading sample data.");
        this.architectsInitializer.run();
        LOG.info("All sample data loaded.");
    }
}
