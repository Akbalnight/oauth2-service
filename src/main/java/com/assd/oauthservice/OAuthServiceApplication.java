package com.assd.oauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;


/**
 * OAuthServiceApplication.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 */
@SpringBootApplication(scanBasePackages = "com.assd.oauthservice", exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class OAuthServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(OAuthServiceApplication.class, args);
    }
}
