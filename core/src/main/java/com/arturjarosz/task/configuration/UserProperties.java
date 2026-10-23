package com.arturjarosz.task.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "task.configuration.default-values")
@Configuration
public class UserProperties {
    // defaults are kept here, so every application using core (e.g. sample-data) gets them without own config
    Double vatTax = 0.23;
    Double incomeTax = 0.18;
    String themeColor = "#465fff";

}
