package com.assd.oauthservice.service;

import com.assd.oauthservice.dto.QueryData;
import com.assd.oauthservice.dto.UserResponseObject;
import com.assd.oauthservice.exceptions.BadRequestException;
import com.assd.oauthservice.exceptions.ForbiddenQueryException;
import com.assd.oauthservice.resourcemanager.ResourceManager;
import com.assd.oauthservice.storage.AccessChecker;
import com.assd.oauthservice.dto.UserDTO;
import com.assd.oauthservice.exceptions.ServiceException;
import com.assd.oauthservice.exceptions.UserNotFoundException;
import com.assd.oauthservice.userdetails.AssdUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Description: Сервис для работы с пользователями
 * @author AsMatveev
 */
@Service
public class AuthService
{
    @Autowired
    private AccessChecker accessChecker;
    @Autowired
    private DefaultTokenServices tokenServices;
    @Autowired
    private ResourceManager resources;

    /**
     * Проверка доступа пользователя к запросу
     * @param token     Token пользователя в заголовке запроса
     * @param queryData Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public ResponseEntity<?> checkAccess(String token, QueryData queryData) throws ServiceException
    {
        Authentication authentication = tokenServices.loadAuthentication(token);
        return checkAccess(authentication, queryData);
    }

    /**
     * Проверка доступа пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param queryData      Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public ResponseEntity<?> checkAccess(Authentication authentication, QueryData queryData)
            throws ServiceException
    {
        AssdUserDetails userData = findUserData(authentication);

        if (accessChecker.isHaveAccess(userData.getPermissions(), queryData))
        {
            return new ResponseEntity<>(UserResponseObject.builder()
                                                          .username(userData.getName())
                                                          .id(userData.getUserId())
                                                          .roles(userData.getRoles())
                                                          .build(), HttpStatus.OK);
        }
        throw new ForbiddenQueryException(resources.getResource(ResourceManager.FORBIDDEN_QUERY, authentication.getName(),
                queryData.getMethod(), queryData.getPath()));
    }

    /**
     * Удаляет токены текущего пользователя
     * @param authentication Данные аутентификации пользователя
     */
    public void logout(OAuth2Authentication authentication)
    {
        final String userToken = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        tokenServices.revokeToken(userToken);
    }

    /**
     * Возвращает всех пользователей с активными токенами доступа в рамках сервиса клиента
     * @param authentication Данные клиента
     * @return Возвращает список активных пользователей
     * @throws ServiceException Исключение если у текущего пользователя нет доступа к методу
     */
    public List<UserDTO> getActiveUsers(Authentication authentication)
            throws ServiceException
    {
        return null;
//        return tokenStore.getActiveUsers(authentication.getOAuth2Request()
//                                                       .getClientId());
    }

    /**
     * Удаляет accessToken и refreshToken указанного пользователя
     * @param clientId Id клиента текущего пользователя
     * @param username Логин пользователя для удаления токенов
     * @param userId   Id пользователя для удаления токенов
     * @throws ServiceException Исключение если пользователь не найден
     */
    public void revokeTokenForUser(String clientId, String username, Integer userId)
            throws ServiceException
    {
        if (username == null)
        {
            throw new BadRequestException(resources.getResource(ResourceManager.USER_NAME_NOT_SPECIFIED));
        }

        OAuth2AccessToken token = null; //tokenStore.getTokenForUser(clientId, username, userId);
        if (token == null)
        {
            throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, username));
        }
        tokenServices.revokeToken(token.getValue());
    }

    /**
     * Проверяет доступ пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param query          Данные запроса
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public void verifyAccess(Authentication authentication, QueryData query)
            throws ServiceException
    {
        AssdUserDetails currentUser = findUserData(authentication);
        if (!accessChecker.isHaveAccess(currentUser.getPermissions(), query))
        {
            throw new ForbiddenQueryException(resources.getResource(ResourceManager.FORBIDDEN_QUERY, currentUser.getName(),
                    query.getMethod(), query.getPath()));
        }
    }

    /**
     * Возвращет объект {@link AssdUserDetails} текущего пользователя
     * @param authentication Данные аутентификации пользователя
     * @return Возвращает данные пользователя
     * @throws UserNotFoundException Исключение при ошибке получения данных аутентификации пользователя
     */
    private AssdUserDetails findUserData(Authentication authentication)
            throws UserNotFoundException
    {
        if (authentication.getPrincipal() instanceof AssdUserDetails)
        {
            return (AssdUserDetails) authentication.getPrincipal();
        }
        throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, authentication.getName()));
    }
}
