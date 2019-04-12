package com.esb.oauthservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;

import com.esb.oauthservice.mongo.MongoTokenStore;

/**
 * TokenStoreConfig.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Настройки хранения токенов
 */
@Configuration
public class TokenStoreConfig
{
    @Bean
    public TokenStore tokenStore()
    {
        return new MongoTokenStore();
    }

    @Bean
    public DefaultTokenServices tokenServices()
    {
        DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
        defaultTokenServices.setTokenStore(tokenStore());
        defaultTokenServices.setSupportRefreshToken(true);
        return defaultTokenServices;
    }
}
