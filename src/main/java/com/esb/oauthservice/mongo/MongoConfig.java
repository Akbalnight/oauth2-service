package com.esb.oauthservice.mongo;

import com.esb.oauthservice.datasource.DatabaseSettings;
import com.esb.oauthservice.datasource.DatabasesConfig;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDbFactory;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.SimpleMongoDbFactory;

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

    @Bean
    public MongoTemplate mongoTemplate()
    {
        return new MongoTemplate(mongoDbFactory());
    }
}