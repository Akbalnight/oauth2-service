package com.esb.oauthservice.service;

import com.esb.oauthservice.dto.QueryData;
import com.esb.oauthservice.dto.UserDTO;
import com.esb.oauthservice.dto.UserResponseObject;
import com.esb.oauthservice.exceptions.BadRequestException;
import com.esb.oauthservice.exceptions.ForbiddenQueryException;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.exceptions.UserNotFoundException;
import com.esb.oauthservice.mongo.MongoTokenStore;
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
                                                          .id(userData.getUserId())
                                                          .roles(userData.getRoles())
                                                          .build(), HttpStatus.OK);
        }
        throw new ForbiddenQueryException(authentication.getName(), queryData);
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
            throw new BadRequestException("Имя пользователя не указано!");
        }

        OAuth2AccessToken token = tokenStore.getTokenForUser(clientId, username, userId);
        if (token == null)
        {
            throw new UserNotFoundException(username);
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
            throw new ForbiddenQueryException(currentUser.getName(), query);
        }
    }

    /**
     * Возвращет объект {@link EsbUserDetails} текущего пользователя
     * @param authentication Данные аутентификации пользователя
     * @return Возвращает данные пользователя
     * @throws UserNotFoundException
     */
    private EsbUserDetails findUserData(OAuth2Authentication authentication)
            throws UserNotFoundException
    {
        if (authentication.getPrincipal() instanceof EsbUserDetails)
        {
            return (EsbUserDetails) authentication.getPrincipal();
        }
        throw new UserNotFoundException(authentication.getName());
    }
}
