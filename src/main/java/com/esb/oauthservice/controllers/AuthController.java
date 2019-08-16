package com.esb.oauthservice.controllers;

import com.esb.oauthservice.dto.ActiveUser;
import com.esb.oauthservice.exceptions.ForbiddenQueryException;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.dto.QueryData;
import com.esb.oauthservice.mongo.MongoTokenStore;
import com.esb.oauthservice.storage.AccessChecker;
import com.esb.oauthservice.storage.UserData;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
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
    private UsersStorage usersStorage;
    @Autowired
    private AccessChecker accessChecker;
    @Autowired
    private MongoTokenStore tokenStore;
    @Autowired
    private DefaultTokenServices tokenServices;

    @PostMapping(value = "/checkAccess")
    public ResponseEntity<?> checkAccess(OAuth2Authentication authentication, @RequestBody QueryData queryData)
            throws ServiceException
    {
        UserData userData = findUserData(authentication);
        if (accessChecker.isHaveAccess(userData.getPermissions(), queryData))
        {
            return new ResponseEntity<>(userData.getUserResponseObject(), HttpStatus.OK);
        }
        throw new ForbiddenQueryException(authentication.getName(), queryData);
    }

    private UserData findUserData(OAuth2Authentication authentication)
    {
        if (!usersStorage.isUserExist(authentication))
        {
            usersStorage.addUser(authentication);
        }
        return usersStorage.getUser(authentication);
    }

    @GetMapping("/revokeToken")
    @ResponseStatus(code = HttpStatus.OK)
    private void logout(OAuth2Authentication authentication)
    {
        final String userToken = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        tokenServices.revokeToken(userToken);
        //tokenStore.removeRefreshToken(userToken);//TODO проверить что refresh токен тоже удаляется из бд
        usersStorage.removeUser(authentication);
    }

    /**
     * Возвращает всех пользователей с активными токенами доступа в рамках сервиса клиента
     * @param authentication Данные клиента
     * @return Возвращает список активных пользователей
     * @throws ServiceException Исключение елси у текущего пользователя нет доступа к методу
     */
    @GetMapping(value = "/activeUsers")
    public List<ActiveUser> getActiveUsers(OAuth2Authentication authentication)
            throws ServiceException
    {
        UserData userData = findUserData(authentication);
        QueryData query = QueryData.builder()
                                   .method(HttpMethod.GET)
                                   .path("/activeUsers")
                                   .build();
        if (!accessChecker.isHaveAccess(userData.getPermissions(), query))
        {
            throw new ForbiddenQueryException(authentication.getName(), query);
        }
        return tokenStore.getActiveUsers(authentication.getOAuth2Request().getClientId());
    }
}
