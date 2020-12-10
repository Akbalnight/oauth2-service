package com.assd.oauthservice.config;

import com.assd.oauthservice.ldap.AssdLdapUserDetailsContextMapper;
import com.assd.oauthservice.userdetails.AssdDbUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

import javax.sql.DataSource;

/**
 * SecurityConfig.java
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Настройки конфигурации аутентификации/авторизации пользователей
 */
@Configuration
@EnableWebSecurity
//@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{
    @Value("${spring.ldap.urls}")
    private String ldapUrl;

    @Value("${spring.ldap.domain}")
    private String ldapDomain;

    private static final String SQL_GET_USERS_BY_NAME = "select username, password, enabled from users where username=LOWER(?)";
    private static final String SQL_AUTORITIES_BY_NAME = "" +
            "select user_id, c.name " +
            "   from users a \n" +
            "   join user_roles b on b.user_id = a.id \n" +
            "   join roles c on c.id = b.role_id \n" +
            "where a.username = LOWER(?)";

    @Autowired
    private DataSource dataSource;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth)
    {
        auth.authenticationProvider(daoAuthenticationProvider());
        if (!ldapUrl.isEmpty() && !ldapDomain.isEmpty())
        {
            auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
        }
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception
    {
        http
                .requestMatchers()
                .antMatchers("/oauth/login", "/oauth/logout", "/oauth/authorize")
//            .authorizeRequests()
//                .antMatchers(
//                        "/login",
//                        "/bootstrap-4.3.1/**",
//                        "/css/**",
//                        "/img/**",
//                        "/oauth/authorize").permitAll()
            .and()
                .authorizeRequests().anyRequest().authenticated()
            .and()
                .csrf().disable()
//            .and()
                .formLogin()
                .loginPage("/oauth/login")
                .defaultSuccessUrl("/oauth/home")
                .permitAll()
            .and()
                .logout()
                .logoutUrl("/oauth/logout")
                .deleteCookies("JSESSIONID")
                .permitAll()
            .and()
                .exceptionHandling()
                .accessDeniedHandler(new OAuth2AccessDeniedHandler());

        // Необходимо для получения списка активных сессий
//        http.sessionManagement().sess
    }

    /**
     * Конфигурация провайдера аутентификации из БД
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider()
    {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(assdDbUserDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * {@code UserDetailsService} из БД
     */
    @Bean
    public UserDetailsService assdDbUserDetailsService()
    {
        AssdDbUserDetailsService service = new AssdDbUserDetailsService();
//        service.setDataSource(jdbcDataSource());
        service.setDataSource(dataSource);
        service.setUsersByUsernameQuery(SQL_GET_USERS_BY_NAME);
        service.setAuthoritiesByUsernameQuery(SQL_AUTORITIES_BY_NAME);
        return service;
    }

    /**
     * Провайдер LDAP аутентификации
     * @return {@link AuthenticationProvider}
     */
    @Bean
    public AuthenticationProvider activeDirectoryLdapAuthenticationProvider()
    {
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(ldapDomain,
                ldapUrl);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserDetailsContextMapper(userDetailsContextMapper());
        return provider;
    }

    /**
     * База данных пользователей
     */
//    @Bean
//    public DataSource jdbcDataSource()
//    {
//        return dataSource;
//    }

    @Override
    @Bean
    public AuthenticationManager authenticationManagerBean()
            throws Exception
    {
        return super.authenticationManagerBean();
    }

    @Bean(name = "passwordEncoder")
    public PasswordEncoder passwordEncoder()
    {
        // в БД хранятся пароли в виде хэшей BCrypt
        return new BCryptPasswordEncoder();
    }

    /**
     * Получение информации о пользователе из LDAP
     */
    @Bean
    public UserDetailsContextMapper userDetailsContextMapper()
    {
        return new AssdLdapUserDetailsContextMapper(ldapDomain);
    }
}
