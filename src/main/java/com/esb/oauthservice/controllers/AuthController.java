package com.esb.oauthservice.controllers;

import com.esb.oauthservice.model.QueryData;
import com.esb.oauthservice.storage.Permission;
import com.esb.oauthservice.storage.UserData;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
    private UsersStorage usersStorage;
    private AntPathMatcher matcher = new AntPathMatcher();

    @PostMapping(value = "/checkAccess")
    public ResponseEntity<?> checkAccess(OAuth2Authentication authentication, @RequestBody QueryData queryData)
    {
        UserData userData = usersStorage.getUser(authentication);
        if (isHaveAccess(userData.getPermissions(), queryData))
        {
            return new ResponseEntity<>(userData.getUserResponseObject(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
    }

    private boolean isHaveAccess(List<Permission> permissions, QueryData queryData)
    {
        return matchPermission(permissions, queryData.getMethod(), queryData.getPath());
    }

    private boolean matchPermission(List<Permission> permissions, HttpMethod method, String path)
    {
        return permissions.stream()
                          .filter(permission -> permission.getMethod() == method && matcher.match(permission.getPath(), path))
                          .findAny()
                          .isPresent();
    }
}
