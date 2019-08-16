package com.esb.oauthservice.config;

import com.esb.oauthservice.datasource.DatabaseSettings;
import com.esb.oauthservice.datasource.DatabasesConfig;
import com.esb.oauthservice.mongo.UsernamePasswordAuthenticationTokenConverter;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.singletonList;

@Configuration
public class MongoConfig
{
    public static final String DB_NAME = "mongo_oauth2";

    @Autowired
    private DatabasesConfig config;

    @Bean
    public MongoDbFactory mongoDbFactory()
    {
        DatabaseSettings dbSettings = config.getDatabaseSettings(DB_NAME);
        MongoClient mongoClient;
        if (dbSettings.getUsername() == null || dbSettings.getUsername().isEmpty())
        {
            mongoClient = new MongoClient(new ServerAddress(dbSettings.getUrl()));
        }
        else
        {
            mongoClient = new MongoClient(singletonList(new ServerAddress(dbSettings.getUrl())),
                    singletonList(MongoCredential.createCredential(dbSettings.getUsername(), DB_NAME,
                            dbSettings.getPassword().toCharArray())));
        }
        return new SimpleMongoDbFactory(mongoClient, DB_NAME);
    }

    /**
     * Парсинг объектов при чтении из базы
     */
    @Bean
    public MongoCustomConversions customConversions()
    {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new UsernamePasswordAuthenticationTokenConverter());
        return new MongoCustomConversions(converters);
    }

    @Bean
    public MongoTemplate mongoTemplate()
    {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoDbFactory());
        MappingMongoConverter mongoMapping = (MappingMongoConverter) mongoTemplate.getConverter();
        mongoMapping.setCustomConversions(customConversions());
        mongoMapping.afterPropertiesSet();
        return mongoTemplate;
    }
}