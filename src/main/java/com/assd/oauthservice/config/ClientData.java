package com.assd.oauthservice.config;

import org.springframework.beans.factory.annotation.Value;

/**
 * Const.java
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Константы с данными сервисов
 */
public class ClientData
{
//    public static final String CLIENT_ID = "ASKUTE-service";
//    public static final String CLIENT_SECRET = "$2a$10$gQxHepiCTQ2OkFG2yldYdOFpXlCPBKKPOMv5HB9O0evnjB0iW8EjO"; // ASKUTE-password в Bcrypt
//    public static final String CLIENT_SECRET = "ASKUTE-password"; // ASKUTE-password в Bcrypt

    /**
     * Параметры пользователя для проверки токена
     */
    @Value("${auth.clientId}")
    public static String CLIENT_ID;

    @Value("${auth.clientSecret}")
    public static String CLIENT_SECRET;

}
