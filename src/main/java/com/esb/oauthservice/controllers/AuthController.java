package com.esb.oauthservice.controllers;

import com.esb.oauthservice.config.Const;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.model.QueryData;
import com.esb.oauthservice.storage.UserResponseObject;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
public class AuthController
{
    @Autowired
    private UsersStorage usersStorage;

    @Autowired
    private DefaultTokenServices tokenServices;

    @GetMapping(value = "/auth")
    //@Secured({"ROLE_USER", "ROLE_ADMIN"})
    public String auth(Principal principal/*, OAuth2AuthorizedClient authorizedClient*/)
    {
        if (principal != null)
        {
            return principal.getName() + " " + principal.toString();
        }
        return "false";
    }

    @PostMapping(value = "/checkAccess")
    public ResponseEntity<?> checkAccess(Principal principal, @RequestBody QueryData queryData)
            throws ServiceException
    {
        if (queryData == null || queryData.getMethod() == null || queryData.getMethod() == null)
        {
            return new ResponseEntity("{\"error\": \""+HttpStatus.BAD_REQUEST.getReasonPhrase()+"\",\"error_description\":\"Не " +
                    "указаны параметры " +
                    "запроса!\"}", HttpStatus.BAD_REQUEST);
        }

        UserResponseObject result = usersStorage.checkAccess(principal.getName(), queryData.getMethod(),
                queryData.getPath());
        if (result != null)
        {
            return new ResponseEntity(result, HttpStatus.OK);
        }
        else
        {
            return new ResponseEntity(HttpStatus.FORBIDDEN);
        }

       /* {
            "error": "invalid_token",
                "error_description": "Invalid access token: caa0ff6b-29d1-45e1-a416-2785554647b5g"
        }*/
    }

    @GetMapping("/revokeToken")//TODO заменить на /logout
    @ResponseStatus(code = HttpStatus.OK)
    private void logout(Authentication authentication)
    {
        final String userToken = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        tokenServices.revokeToken(userToken);
        //tokenStore.removeRefreshToken(userToken);//TODO проверить что refresh токен тоже удаляется из бд
        usersStorage.removeUser(authentication.getName());
    }

    @Resource(name = "tokenStore")
    TokenStore tokenStore;
    @GetMapping(value = "/activeUsers")
    @ResponseBody
    /**
     * Возвращает список всех пользователей
     */
    public List<String> getActiveUsers(Principal principal)
            throws ServiceException
    {
        List<String> tokenValues = new ArrayList<>();
        if (usersStorage.checkAccess(principal.getName(), HttpMethod.GET, "/activeUsers") != null)
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
