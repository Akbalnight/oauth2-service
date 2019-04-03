package com.esb.oauthservice.config;

import com.esb.oauthservice.datasource.DataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.sql.DataSource;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true, securedEnabled = true)
public class SecurityConfig
        extends WebSecurityConfigurerAdapter
{
    private static String SQL_GET_USERS_BY_NAME = "select username,password, enabled from users where username=LOWER" +
            "(?)";
    private static String SQL_AUTORITIES_BY_NAME = "select username, role from user_roles where username=LOWER(?)";

    @Autowired
    private DataSourceManager dataSourceManager;

    @Bean
    public DataSource jdbcDataSource()
    {
        return dataSourceManager.getDataSource("auth");
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
    {/*
        return new PasswordEncoder()
        {
            public String encode(CharSequence charSequence)
            {
                return charSequence.toString();
            }

            public boolean matches(CharSequence charSequence, String s)
            {
                return true;
            }
        };*/
        // в БД хранятся пароли в виде хэшей BCrypt
        return new BCryptPasswordEncoder();
    }

    @Autowired
    public void configAuthentication(AuthenticationManagerBuilder auth)
            throws Exception
    {
        auth.jdbcAuthentication().dataSource(jdbcDataSource()).passwordEncoder(passwordEncoder())
         .usersByUsernameQuery(SQL_GET_USERS_BY_NAME).authoritiesByUsernameQuery(SQL_AUTORITIES_BY_NAME);
    }

}
