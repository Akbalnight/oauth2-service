package com.assd.oauthservice.config;

import com.assd.oauthservice.config.pkce.PkceAuthorizationCodeServices;
import com.assd.oauthservice.config.pkce.PkceAuthorizationCodeTokenGranter;
import com.assd.oauthservice.token.AssdTokenEnhancer;
import com.assd.oauthservice.token.AssdTokenService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.CompositeTokenGranter;
import org.springframework.security.oauth2.provider.OAuth2RequestFactory;
import org.springframework.security.oauth2.provider.TokenGranter;
import org.springframework.security.oauth2.provider.client.ClientCredentialsTokenGranter;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.implicit.ImplicitTokenGranter;
import org.springframework.security.oauth2.provider.password.ResourceOwnerPasswordTokenGranter;
import org.springframework.security.oauth2.provider.refresh.RefreshTokenGranter;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;
import org.springframework.security.web.servletapi.SecurityContextHolderAwareRequestFilter;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * AuthorizationServerConfig.java
 * Date: 6 may 2020 г.
 * Users: av.eliseev
 * Description: Настройка конфигурации сервера авторизации токенов
 */
@Log4j2
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig
        extends AuthorizationServerConfigurerAdapter
{
    private static final String GRANT_TYPE_AUTHORIZATION_CODE = "authorization_code";
    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String REFRESH_TOKEN = "refresh_token";
    private static final String SCOPE_READ = "read";
    private static final String SCOPE_WRITE = "write";
    private static final String TRUST = "trust";
    private static final String RESOURCE_ID = "resource_id";

    /**
     * Продолжительность жизни токена в секундах
     */
    @Value("${auth.access_token.expired.seconds}")
    private int accessTokenExpiredSeconds;

    /**
     * Продолжительность жизни refresh токена в секундах
     */
    @Value("${auth.refresh_token.expired.seconds}")
    private int refreshTokenExpiredSeconds;

    /**
     * Параметры пользователя для проверки токена
     */
    @Value("${auth.clientId}")
    public String CLIENT_ID;

    @Value("${auth.clientSecret}")
    public String CLIENT_SECRET;

    @Autowired
    LoadProps loadProps;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenStore tokenStore;

    private DataSource dataSource;

    @Autowired
    private PasswordEncoder passwordEncoder;


    @Autowired
    public AuthorizationServerConfig(DataSource dataSource){
        this.dataSource = dataSource;
    }

    @Bean
    public TokenStore tokenStore() {
        return new JdbcTokenStore(dataSource);
    }

    @Bean
    public SecurityContextHolderAwareRequestFilter securityContextHolderAwareRequestFilter() {
        return new SecurityContextHolderAwareRequestFilter();
    }

    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) {
        security.allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clients)
            throws Exception
    {
        log.info("redirectUris => {}", String.join(", ", loadProps.getRedirectUrls()));
        clients.inMemory()
                .withClient(CLIENT_ID)
                .secret(passwordEncoder.encode(CLIENT_SECRET))
                .redirectUris(loadProps.getRedirectUrls())
                .authorizedGrantTypes(GRANT_TYPE_AUTHORIZATION_CODE, REFRESH_TOKEN)
                .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
                .autoApprove(true)
//                    .and()
//                    .withClient(ClientData.CLIENT_ID)
//                    .secret(passwordEncoder.encode(ClientData.CLIENT_SECRET))
//                    .authorizedGrantTypes(GRANT_TYPE_PASSWORD)
//                    .scopes(SCOPE_READ, SCOPE_WRITE, TRUST)
                .accessTokenValiditySeconds(accessTokenExpiredSeconds)
                .refreshTokenValiditySeconds(refreshTokenExpiredSeconds);
    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints)
    {
        AssdTokenService tokenServices = tokenServices();
        tokenServices.setClientDetailsService(endpoints.getClientDetailsService());
        endpoints
                .tokenServices(tokenServices)
                .tokenStore(tokenStore)
                .tokenEnhancer(tokenEnhancer())
                .authenticationManager(authenticationManager)
                .authorizationCodeServices(new PkceAuthorizationCodeServices(endpoints.getClientDetailsService(), passwordEncoder))
                .tokenGranter(tokenGranter(endpoints));
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

    private TokenGranter tokenGranter(final AuthorizationServerEndpointsConfigurer endpoints) {
//        List<TokenGranter> granters = new ArrayList<>(Collections.singletonList(endpoints.getTokenGranter()));
        List<TokenGranter> granters = new ArrayList<>();


        AuthorizationServerTokenServices tokenServices = endpoints.getTokenServices();
        AuthorizationCodeServices authorizationCodeServices = endpoints.getAuthorizationCodeServices();
        ClientDetailsService clientDetailsService = endpoints.getClientDetailsService();
        OAuth2RequestFactory requestFactory = endpoints.getOAuth2RequestFactory();

        granters.add(new RefreshTokenGranter(tokenServices, clientDetailsService, requestFactory));
        granters.add(new ImplicitTokenGranter(tokenServices, clientDetailsService, requestFactory));
        granters.add(new ClientCredentialsTokenGranter(tokenServices, clientDetailsService, requestFactory));
        granters.add(new ResourceOwnerPasswordTokenGranter(authenticationManager, tokenServices, clientDetailsService, requestFactory));
        granters.add(new PkceAuthorizationCodeTokenGranter(tokenServices, ((PkceAuthorizationCodeServices) authorizationCodeServices), clientDetailsService, requestFactory));

        return new CompositeTokenGranter(granters);
    }
}
