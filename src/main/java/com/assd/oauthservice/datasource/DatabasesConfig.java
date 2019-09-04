package com.assd.oauthservice.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

/**
 * DatabasesConfig.java
 * Date: 10 сент. 2018 г.
 * Users: amatveev
 * Description: Класс с конфигурациями баз данных
 */
@Configuration
@ConfigurationProperties
@EnableConfigurationProperties
@PropertySource("application.yml")
public class DatabasesConfig
{
    private final Map<String, DatabaseSettings> databases = new HashMap<>();
    private final Map<String, String> config = new HashMap<>();

    /**
     * Возвращает сопоставление сервисов и названий их конфигурации БД
     *
     * @return возвращает сопоставление сервисов и названий их конфигурации БД
     */
    public Map<String, String> getConfig()
    {
        return config;
    }

    /**
     * Возвращает сопоставление названий конфигураций БД и их парамметры конфигурации
     *
     * @return возвращает сопоставление названий конфигураций БД и их парамметры конфигурации
     */
    public Map<String, DatabaseSettings> getDatabases()
    {
        return this.databases;
    }

    /**
     * Возвращает парамметры конфигурации БД
     *
     * @param name название конфигурации БД
     * @return возвращает парамметры конфигурации БД
     */
    public DatabaseSettings getDatabaseSettings(String name)
    {
        return databases.get(config.get(name));
    }
}
