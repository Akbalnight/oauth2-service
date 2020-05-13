package com.assd.oauthservice.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.error.OAuth2AccessDeniedHandler;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * ResourceServerConfig.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Настройки конфигурации сервера ресурсов
 */
//@Configuration
//@EnableResourceServer
//public class ResourceServerConfig
//        extends ResourceServerConfigurerAdapter
//{
//
//    private static final String RESOURCE_ID = "resource_id";
//
//    @Override
//    public void configure(ResourceServerSecurityConfigurer resources)
//    {
//        resources
//                .resourceId(RESOURCE_ID)
//                .stateless(false);
//    }
//
//    @Override
//    public void configure(HttpSecurity http) throws Exception {
//        http
//            .requestMatchers()
//            .antMatchers("/login", "/oauth/authorize")
//            .and()
//            .authorizeRequests()
//            .antMatchers(
//                    "/login",
//                    "/bootstrap-4.3.1/**",
//                    "/css/**",
//                    "/img/**",
//                    "/oauth/authorize").permitAll()
//            .and()
//                .authorizeRequests()
//                .antMatchers("/oauth/checkAccess", "/oauth/revokeToken", "/oauth/revokeTokenForUser", "/oauth/activeUsers")
//                .authenticated()
////            .and()
////                .authorizeRequests().anyRequest().authenticated()
//            .and()
//                .csrf()
//            .and()
//                .formLogin()
//                .loginPage("/login")
//                .permitAll()
//            .and()
//                .exceptionHandling()
//                .accessDeniedHandler(new OAuth2AccessDeniedHandler());
//    }
//}
