package com.assd.oauthservice.config;

import com.assd.oauthservice.token.AssdTokenEnhancer;
import com.assd.oauthservice.token.AssdTokenService;
import com.assd.oauthservice.token.JdbcTokenStores;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;

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
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String TRUST = "trust";

    /**
     * Продолжительность жизни токена в секундах
     */
    @Value("${token.expired.seconds:3600}")
    private int tokenExpiredSeconds;

    /**
     * Продолжительность жизни refresh токена в секундах
     */
    @Value("${refreshtoken.expired.seconds:32400}")
    private int refreshTokenExpiredSeconds;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenStore tokenStore;

//    @Autowired
    private DataSource dataSource;

    @Autowired
    public AuthorizationServerConfig(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Bean
    public TokenStore tokenStore() {
//        DataSource tokenDataSource = DataSourceBuilder.create()
//                .driverClassName("org.postgresql.Driver")
//                .username("postgres")
//                .password("postgres")
//                .url("jdbc:postgresql://10.5.121.117:5432/auth")
//                .build();
//        return new JdbcTokenStore(tokenDataSource);
        return new JdbcTokenStore(dataSource);
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception
    {
        clients.inMemory()
               .withClient(ClientData.CLIENT_ID)
               .secret(ClientData.CLIENT_SECRET)
               .authorizedGrantTypes(GRANT_TYPE_PASSWORD, REFRESH_TOKEN)
               .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
               .accessTokenValiditySeconds(tokenExpiredSeconds)
               .refreshTokenValiditySeconds(refreshTokenExpiredSeconds);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
    {
        AssdTokenService tokenServices = tokenServices();
        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        endpoints
                .authenticationManager(authenticationManager)
                .tokenStore(tokenStore)
                .tokenServices(tokenServices)
                .tokenEnhancer(tokenEnhancer());
    }

    @Bean
    @Primary
    public AssdTokenService tokenServices()
    {
        AssdTokenService tokenServices = new AssdTokenService();
        tokenServices.setTokenStore(tokenStore());
        tokenServices.setSupportRefreshToken(true);
        tokenServices.setTokenEnhancer(tokenEnhancer());
        return tokenServices;
    }

    @Bean
    public TokenEnhancer tokenEnhancer()
    {
        return new AssdTokenEnhancer();
    }

//    @Bean
//    public TokenStore tokenStore()
//    {
//        return new MongoTokenStore();
//    }

//    @Bean
//    public TokenStore tokenStore() {
//        return new JdbcTokenStore(dataSourceManager.getDataSource("users"));
//    }
}
