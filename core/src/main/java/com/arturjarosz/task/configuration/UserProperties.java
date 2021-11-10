package com.arturjarosz.task.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:preferences.yaml", factory = YamlPropertiesFactory.class)
@ConfigurationProperties(prefix = "user-preferences")
@Configuration
public class UserProperties {
    String vatTax;
    String incomeTax;

    public String getVatTax() {
        return this.vatTax;
    }

    public void setVatTax(String vatTax) {
        this.vatTax = vatTax;
    }

    public String getIncomeTax() {
        return this.incomeTax;
    }

    public void setIncomeTax(String incomeTax) {
        this.incomeTax = incomeTax;
    }
}

