package com.arturjarosz.task.sharedkernel.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;

import java.util.Locale;

@Configuration
public class BaseConfiguration {

    /*    private final String defaultLanguageCode;
        private final String defaultEncoding;*/
    private final String[] messageSources = {
            "classpath:/i18n/base/base",
            "classpath:/i18n/client/client",
            "classpath:/i18n/architect/architect",
    };

    public BaseConfiguration() {
/*        this.defaultLanguageCode = language;
        this.defaultEncoding = encoding;*/
    }

    @Bean
    public ReloadableResourceBundleMessageSource messageSource() {
        ReloadableResourceBundleMessageSource bundleMessageSource = new ReloadableResourceBundleMessageSource();
        bundleMessageSource.setBasenames(this.messageSources);
        bundleMessageSource.setDefaultEncoding("UTF-8");
        Locale.setDefault(Locale.forLanguageTag("en"));
        return bundleMessageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        return localeResolver;
    }

}
