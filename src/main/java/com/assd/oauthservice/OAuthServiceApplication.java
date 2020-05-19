package com.assd.oauthservice;

import lombok.extern.log4j.Log4j2;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;


/**
 * OAuthServiceApplication.java
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 */
@Log4j2
@SpringBootApplication(scanBasePackages = "com.assd.oauthservice", exclude = {SecurityAutoConfiguration.class})
@EnableScheduling
public class OAuthServiceApplication
{
    @Scheduled(fixedRate = 5000)
    private void updateConfig(){
        log.info("Log Oauth");
    }
    public static void main(String[] args)
    {
        SpringApplication.run(OAuthServiceApplication.class, args);
    }
}
