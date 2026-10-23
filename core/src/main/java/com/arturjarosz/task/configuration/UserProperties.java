package com.arturjarosz.task.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@ConfigurationProperties(prefix = "task.configuration.default-values")
@Configuration
public class UserProperties {
    Double vatTax;
    Double incomeTax;
    String themeColor;

}
