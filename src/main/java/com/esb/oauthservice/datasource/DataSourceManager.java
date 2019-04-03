package com.esb.oauthservice.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * DataSourceManager.java
 * Date: 10 сент. 2018 г.
 * Users: amatveev
 * Description: Менеджер конфигураций баз данных
 */
@Component
public class DataSourceManager
{
    private Map<String, DataSource> dataSources = new HashMap<>();

    @Autowired
    private DatabasesConfig config;

    /**
     * Созданее пула подключений к указанной БД
     *
     * @param settings парамметры подключения к БД
     * @return пул подключений к БД
     */
    private DataSource createHikariDataSource(DatabaseSettings settings)
    {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(settings.getDriver());
        hikariConfig.setJdbcUrl(settings.getUrl());
        hikariConfig.setUsername(settings.getUsername());
        hikariConfig.setPassword(settings.getPassword());
        hikariConfig.setMaximumPoolSize(settings.getPoolSize());
        hikariConfig.setPoolName("main");
        return new HikariDataSource(hikariConfig);
    }

    /**
     * Возвращает пул подключений к БД по указанному названию БД
     *
     * @param name название конфигурации для подключения к БД
     * @return возвращает пул подключений к БД
     */
    public DataSource getDataSource(String name)
    {
        return Optional.ofNullable(dataSources.get(name)).orElseGet(() ->
        {
            // Создадим пул подключений
            DataSource dataSource = createHikariDataSource(config.getDatabaseSettings(name));
            dataSources.put(name, dataSource);
            return dataSource;
        });
    }
}
