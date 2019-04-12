package com.esb.oauthservice.config;

import com.esb.oauthservice.datasource.DataSourceManager;
import com.esb.oauthservice.ldap.ESBActiveDirectoryLdapAuthenticationProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

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
    private static String SQL_GET_USERS_BY_NAME =
            "select username,password, enabled from users where username=LOWER(?)";
    private static String SQL_AUTORITIES_BY_NAME = "select username, role from user_roles where username=LOWER(?)";

    @Autowired
    private DataSourceManager dataSourceManager;

    @Bean
    public DataSource jdbcDataSource()
    {
        return dataSourceManager.getDataSource("users");
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

    @Value("${auth.ldap.url:}")
    private String ldapUrl;

    @Value("${auth.ldap.domen:}")
    private String ldapDomen;

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth)
            throws Exception
    {
        auth
                .jdbcAuthentication()
                .dataSource(jdbcDataSource())
                .passwordEncoder(passwordEncoder())
                .usersByUsernameQuery(SQL_GET_USERS_BY_NAME)
                .authoritiesByUsernameQuery(SQL_AUTORITIES_BY_NAME);
        if (!ldapUrl.isEmpty() && !ldapDomen.isEmpty())
        {
            auth.authenticationProvider(activeDirectoryLdapAuthenticationProvider());
        }
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
        return new ESBActiveDirectoryLdapAuthenticationProvider(provider);
    }

}
