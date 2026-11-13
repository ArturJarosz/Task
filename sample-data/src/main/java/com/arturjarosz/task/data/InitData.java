package com.arturjarosz.task.data;

import org.springframework.boot.SpringApplication;
import com.arturjarosz.task.data.initializer.SampleSchemaProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.arturjarosz.task")
@EnableConfigurationProperties(SampleSchemaProperties.class)
public class InitData {

    public static void main(String[] args) {
        SpringApplication.run(InitData.class, args).close();
    }

}

