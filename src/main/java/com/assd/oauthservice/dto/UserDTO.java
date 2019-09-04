package com.assd.oauthservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * Description: DTO авторизованных пользователей
 * @author AsMatveev
 */
@Builder
@Getter
@Setter
public class UserDTO
{
    /**
     * Id пользователя. Может быть null если пользователь LDAP
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Integer id;
    /**
     * Логин пользователя
     */
    private String username;
    /**
     * Серверное время истечения срока действия accessToken
     */
    private Date accessTokenExpiration;
    /**
     * Серверное время истечения срока действия refreshToken
     */
    private Date refreshTokenExpiration;
}
