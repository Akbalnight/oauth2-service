package com.assd.oauthservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * UserResponseObject.java
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Данные пользователя возвращаемые клиенту
 */
@Getter
@Setter
@Builder
public class UserResponseObject
{
    /**
     * Логин пользователя
     */
    private String username;
    /**
     * Id пользователя
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;
    /**
     * Список ролей пользователя
     */
    private List<String> roles;

    private String code_challenge;
}
