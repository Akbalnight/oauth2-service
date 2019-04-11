package com.esb.oauthservice.config;

import com.esb.oauthservice.database.TokenDaoImpl;
import com.esb.oauthservice.datasource.DataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

/**
 * TokenStoreConfig.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Настройки хранения токенов
 */
@Configuration
public class TokenStoreConfig
{
    @Autowired
    private DataSourceManager dataSourceManager;


    public DataSource jdbcAuthDataSource()
    {
        return dataSourceManager.getDataSource(TokenDaoImpl.TOKEN_DB);
    }

    @Bean
    public TokenStore tokenStore()
    {
        return new JdbcTokenStore(jdbcAuthDataSource());
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
