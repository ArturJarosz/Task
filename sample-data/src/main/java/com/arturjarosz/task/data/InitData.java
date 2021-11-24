package com.arturjarosz.task.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.gson.GsonAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseAutoConfiguration;
import org.springframework.context.annotation.AdviceMode;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement(mode = AdviceMode.PROXY)
@SpringBootApplication(scanBasePackages = "com.arturjarosz.task", exclude = {LiquibaseAutoConfiguration.class, GsonAutoConfiguration.class})
public class InitData {

    public static void main(String[] args) {
        SpringApplication.run(InitData.class, args)
                .close();
    }

}

