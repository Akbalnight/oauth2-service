package com.esb.oauthservice.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

/**
 * Permission.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Данные пермиссии
 */
@Getter
@Setter
public class Permission
{
    /**
     * Метод
     */
    private HttpMethod method;
    /**
     * Путь запроса
     */
    private String path;
}
