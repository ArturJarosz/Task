package com.arturjarosz.task.data;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
@ApplicationService
public class SampleDataLoader extends AbstractDataLoader {
    private final ProxyDataLoader dataLoader;

    @Autowired
    public SampleDataLoader(ProxyDataLoader dataLoader) {
        this.dataLoader = dataLoader;
    }

    @Override
    protected void loadData() {
        LOG.info("Loading sample data.");
        this.dataLoader.loadData();
        LOG.info("All sample data loaded.");
    }
}
