package com.assd.oauthservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

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
     * Id пользователя
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UUID id;

    /**
     * Логин пользователя
     */
    private String username;

    /**
     * Список ролей пользователя
     */
    private List<String> roles;

    private String code_challenge;
}
