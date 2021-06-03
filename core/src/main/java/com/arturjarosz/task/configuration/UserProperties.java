package com.arturjarosz.task.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:preferences.yaml", factory = YamlPropertiesFactory.class)
@ConfigurationProperties(prefix = "user-preferences")
@Configuration
public class UserProperties {
    String vat;

    public String getVat() {
        return this.vat;
    }

    public void setVat(String vat) {
        this.vat = vat;
    }
}

