package com.esb.oauthservice.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenStore;

/**
 * AuthorizationServerConfig.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Настройка конфигурации сервера авторизации токенов
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig
        extends AuthorizationServerConfigurerAdapter
{
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String AUTHORIZATION_CODE = "authorization_code";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String TRUST = "trust";

    /**
     * Продолжительность жизни токена в секундах
     */
    @Value("${token.expired.seconds:300}")
    private int tokenExpiredSeconds;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private TokenStore tokenStore;

    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception
    {
        clients
                .inMemory()
                .withClient(Const.CLIENT_ID)
                .secret(Const.CLIENT_SECRET)
                .authorizedGrantTypes(GRANT_TYPE_PASSWORD, AUTHORIZATION_CODE, REFRESH_TOKEN)
                .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
                .accessTokenValiditySeconds(tokenExpiredSeconds)
                .refreshTokenValiditySeconds(tokenExpiredSeconds);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
    {
        endpoints
                .tokenStore(tokenStore)
                .authenticationManager(authManager);
    }
}