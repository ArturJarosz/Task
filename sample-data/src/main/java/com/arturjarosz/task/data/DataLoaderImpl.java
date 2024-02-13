package com.arturjarosz.task.data;

import com.arturjarosz.task.sharedkernel.annotations.ApplicationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/* Service created only to let spring create proxies that can be used for transaction management for called services.*/

@Slf4j
@ApplicationService
public class DataLoaderImpl implements DataLoader {
    private final List<DataInitializer> dataInitializers;

    @Autowired
    public DataLoaderImpl(List<DataInitializer> dataInitializers) {
        this.dataInitializers = dataInitializers;
    }

    @Transactional()
    @Override
    public void loadData() {
        for (DataInitializer dataInitializer : this.dataInitializers) {
            dataInitializer.initializeData();
        }
    }
}
