package com.arturjarosz.task.sharedkernel.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

@Configuration
public class BaseConfiguration {

    private final String defaultEncoding;
    private final String defaultLanguageCode;
    private final String[] messageSources = {
            "classpath:/i18n/base/base",
            "classpath:/i18n/client/client",
            "classpath:/i18n/contractor/contractor",
            "classpath:/i18n/architect/architect",
            "classpath:/i18n/project/project",
            "classpath:/i18n/project/status"
    };

    public BaseConfiguration(@Value("${task.language}") String defaultLanguageCode,
                             @Value("${task.encoding}") String defaultEncoding) {
        this.defaultEncoding = defaultEncoding;
        this.defaultLanguageCode = defaultLanguageCode;
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource bundleMessageSource = new ReloadableResourceBundleMessageSource();
        bundleMessageSource.setBasenames(this.messageSources);
        bundleMessageSource.setDefaultEncoding(this.defaultEncoding);
        Locale.setDefault(Locale.forLanguageTag(this.defaultLanguageCode));
        return bundleMessageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        return localeResolver;
    }

}
