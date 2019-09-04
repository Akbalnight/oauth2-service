package com.assd.oauthservice.controllers;

import com.assd.oauthservice.dto.QueryData;
import com.assd.oauthservice.dto.UserDTO;
import com.assd.oauthservice.exceptions.ServiceException;
import com.assd.oauthservice.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * AuthController.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Основной контроллер сервиса
 */
@RestController
class AuthController
{
    @Autowired
    private AuthService service;

    /**
     * Проверка доступа пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param queryData Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    @PostMapping(value = "/checkAccess")
    public ResponseEntity<?> checkAccess(OAuth2Authentication authentication, @RequestBody QueryData queryData)
            throws ServiceException
    {
        return service.checkAccess(authentication, queryData);
    }

    /**
     * Удаляет токены текущего пользователя
     * @param authentication Данные аутентификации пользователя
     */
    @GetMapping("/revokeToken")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(OAuth2Authentication authentication)
    {
        service.logout(authentication);
    }

    /**
     * Удаляет accessToken и refreshToken указанного пользователя
     * @param authentication Данные аутентификации текущего пользователя
     * @param userData Логин и Id пользователя. Для LDAP пользователей Id = null
     */
    @PostMapping("/revokeTokenForUser")
    public void revokeTokenByUsername(OAuth2Authentication authentication, @RequestBody UserDTO userData)
    {
        service.verifyAccess(authentication,  QueryData.builder()
                                               .method(HttpMethod.POST)
                                               .path("/revokeTokenForUser")
                                               .build());
        service.revokeTokenForUser(authentication.getOAuth2Request().getClientId(), userData.getUsername(), userData.getId());
    }

    /**
     * Возвращает всех пользователей с активными токенами доступа в рамках сервиса клиента
     * @param authentication Данные клиента
     * @return Возвращает список активных пользователей
     * @throws ServiceException Исключение елси у текущего пользователя нет доступа к методу
     */
    @GetMapping(value = "/activeUsers")
    public List<UserDTO> getActiveUsers(OAuth2Authentication authentication)
            throws ServiceException
    {
        service.verifyAccess(authentication, QueryData.builder()
                                            .method(HttpMethod.GET)
                                            .path("/activeUsers")
                                            .build());
        return service.getActiveUsers(authentication);
    }
}
