package com.esb.oauthservice.storage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * UserResponseObject.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Данные пользователя возвращаемые клиенту
 */
@Getter
@Setter
@Builder
public class UserResponseObject
{
    /**
     * Список ролей пользователя
     */
    private List<String> roles;
    /**
     * Id пользователя
     */
    private int id;
}
