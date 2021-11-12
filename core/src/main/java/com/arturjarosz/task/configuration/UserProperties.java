package com.arturjarosz.task.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:preferences.yaml", factory = YamlPropertiesFactory.class)
@ConfigurationProperties(prefix = "user-preferences")
@Configuration
public class UserProperties {
    Double vatTax;
    Double incomeTax;

    public Double getVatTax() {
        return this.vatTax;
    }

    public void setVatTax(Double vatTax) {
        this.vatTax = vatTax;
    }

    public Double getIncomeTax() {
        return this.incomeTax;
    }

    public void setIncomeTax(Double incomeTax) {
        this.incomeTax = incomeTax;
    }
}

