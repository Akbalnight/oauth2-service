package com.assd.oauthservice.storage;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpMethod;

import java.io.Serializable;

/**
 * Permission.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Данные пермиссии
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Permission implements Serializable
{
    /**
     * Метод
     */
    private HttpMethod method;
    /**
     * Путь запроса
     */
    private String path;


    @Override
    public String toString() {
        return method.name() + " --- " + path;
    }
}
