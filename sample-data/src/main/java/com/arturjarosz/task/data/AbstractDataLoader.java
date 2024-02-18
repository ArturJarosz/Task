package com.arturjarosz.task.data;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * Class that should be extended by every class that is responsible for loading sample data.
 */
public abstract class AbstractDataLoader implements ApplicationRunner {

    protected abstract void loadData();

    @Override
    public void run(ApplicationArguments args) {
        this.loadData();
    }
}
