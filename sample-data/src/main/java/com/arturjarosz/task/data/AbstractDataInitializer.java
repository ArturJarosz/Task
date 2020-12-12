package com.arturjarosz.task.data;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

public abstract class AbstractDataInitializer implements ApplicationRunner {

    protected abstract void loadData();

    @Override
    public void run(ApplicationArguments args) {
        this.loadData();
    }
}
