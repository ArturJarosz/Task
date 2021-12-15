package com.arturjarosz.task.configuration;

import org.hibernate.jpa.HibernatePersistenceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;

import java.util.HashMap;
import java.util.Map;

@Configuration
@PropertySource("database.properties")
public class ConfigurationIT {
    private final String databaseUrl;
    private final String databaseDialect;
    private final String databaseType;

    @Autowired
    public ConfigurationIT(@Value("${test.database.url}") String databaseUrl,
            @Value("${test.database.dialect}") String databaseDialect,
            @Value("${test.database.type}") String databaseType) {
        this.databaseDialect = databaseDialect;
        this.databaseUrl = databaseUrl;
        this.databaseType = databaseType;
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory() {
        LocalContainerEntityManagerFactoryBean entityManagerFactoryBean = new LocalContainerEntityManagerFactoryBean();
        entityManagerFactoryBean.setPersistenceProviderClass(HibernatePersistenceProvider.class);
        entityManagerFactoryBean.setPersistenceUnitName("testIT");
        entityManagerFactoryBean.setPackagesToScan("com.arturjarosz");
        Map<String, String> properties = new HashMap<>();
        properties.put("spring.jpa.database", this.databaseType);
        properties.put("spring.jpa.database-platform", this.databaseDialect);
        properties.put("hibernate.dialect", this.databaseDialect);
        properties.put("hibernate.connection.url", this.databaseUrl);
        entityManagerFactoryBean.setJpaPropertyMap(properties);
        return entityManagerFactoryBean;
    }

    @Bean
    @Autowired
    public JpaTransactionManager transactionManager(LocalContainerEntityManagerFactoryBean emf) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(emf.getObject());
        return transactionManager;
    }
}
