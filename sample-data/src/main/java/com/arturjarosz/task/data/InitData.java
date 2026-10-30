package com.arturjarosz.task.data;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.arturjarosz.task")
public class InitData {

    public static void main(String[] args) {
        SpringApplication.run(InitData.class, args).close();
    }

}

