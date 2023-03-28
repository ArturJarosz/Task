package com.arturjarosz.task.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Data
@PropertySource(value = "classpath:preferences.yaml", factory = YamlPropertiesFactory.class)
@ConfigurationProperties(prefix = "user-preferences")
@Configuration
public class UserProperties {
    Double vatTax;
    Double incomeTax;

}

