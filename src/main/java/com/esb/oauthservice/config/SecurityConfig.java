package com.esb.oauthservice.config;

import com.esb.oauthservice.database.UsersDaoImpl;
import com.esb.oauthservice.userdetails.EsbDbUserDetailsService;
import com.esb.oauthservice.datasource.DataSourceManager;
import com.esb.oauthservice.ldap.EsbLdapUserDetailsContextMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;

import javax.sql.DataSource;

/**
 * SecurityConfig.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Настройки конфигурации аутентификации/авторизации пользователей
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{
    @Value("${auth.ldap.url:}")
    private String ldapUrl;

    @Value("${auth.ldap.domen:}")
    private String ldapDomen;

    private static final String SQL_GET_USERS_BY_NAME = "select username,password, enabled from users where username=LOWER(?)";
    private static final String SQL_AUTORITIES_BY_NAME = "select username, role from user_roles where username=LOWER(?)";

    @Autowired
    private DataSourceManager dataSourceManager;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth)
    {
        auth.authenticationProvider(daoAuthenticationProvider());
        if (!ldapUrl.isEmpty() && !ldapDomen.isEmpty())
        {
            auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
        }
    }

    /**
     * Конфигурация провайдера аутентификации из БД
     */
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider()
    {
        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
        daoAuthenticationProvider.setUserDetailsService(esbDbUserDetailsService());
        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
        return daoAuthenticationProvider;
    }

    /**
     * {@code UserDetailsService} из БД
     */
    @Bean
    public UserDetailsService esbDbUserDetailsService()
    {
        EsbDbUserDetailsService service = new EsbDbUserDetailsService();
        service.setDataSource(jdbcDataSource());
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
        ActiveDirectoryLdapAuthenticationProvider provider = new ActiveDirectoryLdapAuthenticationProvider(ldapDomen,
                ldapUrl);
        provider.setConvertSubErrorCodesToExceptions(true);
        provider.setUseAuthenticationRequestCredentials(true);
        provider.setUserDetailsContextMapper(userDetailsContextMapper());
        return provider;
    }

    /**
     * База данных пользователей
     */
    @Bean
    public DataSource jdbcDataSource()
    {
        return dataSourceManager.getDataSource(UsersDaoImpl.DB_NAME);
    }

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
        return new EsbLdapUserDetailsContextMapper();
    }
}
