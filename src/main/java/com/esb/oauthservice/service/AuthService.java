package com.esb.oauthservice.service;

import com.esb.oauthservice.dto.QueryData;
import com.esb.oauthservice.dto.UserDTO;
import com.esb.oauthservice.dto.UserResponseObject;
import com.esb.oauthservice.exceptions.BadRequestException;
import com.esb.oauthservice.exceptions.ForbiddenQueryException;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.exceptions.UserNotFoundException;
import com.esb.oauthservice.mongo.MongoTokenStore;
import com.esb.oauthservice.resourcemanager.ResourceManager;
import com.esb.oauthservice.storage.AccessChecker;
import com.esb.oauthservice.userdetails.EsbUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.stereotype.Service;

import java.util.List;

import static com.esb.oauthservice.resourcemanager.ResourceManager.*;

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
    private MongoTokenStore tokenStore;
    @Autowired
    private DefaultTokenServices tokenServices;
    @Autowired
    private ResourceManager resources;

    /**
     * Проверка доступа пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param queryData      Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public ResponseEntity<?> checkAccess(OAuth2Authentication authentication, QueryData queryData)
            throws ServiceException
    {
        EsbUserDetails userData = findUserData(authentication);
        if (accessChecker.isHaveAccess(userData.getPermissions(), queryData))
        {
            return new ResponseEntity<>(UserResponseObject.builder()
                                                          .username(userData.getName())
                                                          .id(userData.getUserId())
                                                          .roles(userData.getRoles())
                                                          .build(), HttpStatus.OK);
        }
        throw new ForbiddenQueryException(resources.getResource(FORBIDDEN_QUERY, authentication.getName(),
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
    public List<UserDTO> getActiveUsers(OAuth2Authentication authentication)
            throws ServiceException
    {
        return tokenStore.getActiveUsers(authentication.getOAuth2Request()
                                                       .getClientId());
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
            throw new BadRequestException(resources.getResource(USER_NAME_NOT_SPECIFIED));
        }

        OAuth2AccessToken token = tokenStore.getTokenForUser(clientId, username, userId);
        if (token == null)
        {
            throw new UserNotFoundException(resources.getResource(USER_NOT_FOUND, username));
        }
        tokenServices.revokeToken(token.getValue());
    }

    /**
     * Проверяет доступ пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param query          Данные запроса
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public void verifyAccess(OAuth2Authentication authentication, QueryData query)
            throws ServiceException
    {
        EsbUserDetails currentUser = findUserData(authentication);
        if (!accessChecker.isHaveAccess(currentUser.getPermissions(), query))
        {
            throw new ForbiddenQueryException(resources.getResource(FORBIDDEN_QUERY, currentUser.getName(),
                    query.getMethod(), query.getPath()));
        }
    }

    /**
     * Возвращет объект {@link EsbUserDetails} текущего пользователя
     * @param authentication Данные аутентификации пользователя
     * @return Возвращает данные пользователя
     * @throws UserNotFoundException Исключение при ошибке получения данных аутентификации пользователя
     */
    private EsbUserDetails findUserData(OAuth2Authentication authentication)
            throws UserNotFoundException
    {
        if (authentication.getPrincipal() instanceof EsbUserDetails)
        {
            return (EsbUserDetails) authentication.getPrincipal();
        }
        throw new UserNotFoundException(resources.getResource(USER_NOT_FOUND, authentication.getName()));
    }
}
