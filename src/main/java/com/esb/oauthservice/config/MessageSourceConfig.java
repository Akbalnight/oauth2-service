package com.esb.oauthservice.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;

/**
 * Description: Конфигурация менеджера ресурсов
 * @author AsMatveev
 */
@Configuration
public class MessageSourceConfig
{
    /**
     * Настроим ресурсный менеджер, файл с ресурсами, кодировку
     */
    @Bean
    public MessageSource messageSource()
    {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:resources");
        messageSource.setDefaultEncoding("UTF-8");
        return messageSource;
    }
}
