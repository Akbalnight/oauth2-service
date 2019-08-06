package com.esb.oauthservice.controllers;

import com.esb.oauthservice.config.Const;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.logger.Logger;
import com.esb.oauthservice.model.ExceptionResponseObject;
import com.esb.oauthservice.model.QueryData;
import com.esb.oauthservice.storage.UserResponseObject;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * AuthController.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Основной контроллер сервиса
 */
@RestController
public class AuthController
{
    @Autowired
    private Logger logger;
    @Autowired
    private UsersStorage usersStorage;
    @Autowired
    private DefaultTokenServices tokenServices;

    @Resource(name = "tokenStore")
    private TokenStore tokenStore;

    /**
     * Проверка доступа пользователя к запросу
     * @param principal Данные пользователя
     * @param queryData Данные запроса
     * @return Возвращает id и список ролей пользователя в случае успешного доступа к запросу, иначе статус 403
     * @throws ServiceException
     */
    @PostMapping(value = "/checkAccess")
    public ResponseEntity<?> checkAccess(Principal principal, @RequestBody QueryData queryData)
            throws ServiceException
    {
        if (queryData == null || queryData.getMethod() == null)
        {
            return new ResponseEntity<>(ExceptionResponseObject.builder()
                                                               .error(HttpStatus.BAD_REQUEST.getReasonPhrase())
                                                               .error_description("Не указаны параметры запроса!")
                                                               .build(), HttpStatus.BAD_REQUEST);
        }
        if (principal instanceof OAuth2Authentication)
        {
            UserResponseObject result = usersStorage.checkAccess((OAuth2Authentication) principal,
                    queryData.getMethod(), queryData.getPath());
            if (result != null)
            {
                return new ResponseEntity<>(result, HttpStatus.OK);
            }
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    @GetMapping("/revokeToken")
    @ResponseStatus(code = HttpStatus.OK)
    private void logout(Authentication authentication)
    {
        final String userToken = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        tokenServices.revokeToken(userToken);
        //tokenStore.removeRefreshToken(userToken);//TODO проверить что refresh токен тоже удаляется из бд
        usersStorage.removeUser(authentication.getName());
    }

    @GetMapping(value = "/activeUsers")
    @ResponseBody
    /**
     * Возвращает список всех пользователей
     */ public List<String> getActiveUsers(Principal principal)
            throws ServiceException
    {
        //TODO Отладить
        List<String> tokenValues = new ArrayList<>();
        if (usersStorage.checkAccess((OAuth2Authentication)principal, HttpMethod.GET, "/activeUsers") != null)
        {
            Collection<OAuth2AccessToken> tokens = tokenStore.findTokensByClientId(Const.CLIENT_ID);
            if (tokens != null)
            {
                for (OAuth2AccessToken token : tokens)
                {
                    tokenValues.add(token.getValue());
                }
            }
        }
        return tokenValues;
    }
}
