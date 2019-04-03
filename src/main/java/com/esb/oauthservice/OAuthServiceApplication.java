package com.esb.oauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.esb.oauthservice", exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class OAuthServiceApplication
{
    public static void main(String[] args)
    {
        SpringApplication.run(OAuthServiceApplication.class, args);
    }
}
