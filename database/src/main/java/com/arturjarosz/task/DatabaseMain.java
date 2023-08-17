package com.arturjarosz.task;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication()
public class DatabaseMain {
    public static void main(String[] args) {
        SpringApplication.run(DatabaseMain.class, args).close();
    }
}
