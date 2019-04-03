package com.esb.oauthservice.storage;

import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class UsersStorage
{
    private Map<String, UserPermissions> userPermissions = new HashMap<>();
    private AntPathMatcher matcher = new AntPathMatcher();

    public boolean checkAccess(String login, HttpMethod method, String path)
    {
        if (!userPermissions.containsKey(login))
        {
            fillUserPermissions(login);
        }
        UserPermissions permissions = userPermissions.get(login);

        return checkAccess(permissions.getPermissions(), method, path);
    }

    private boolean checkAccess(List<Permission> permissions, HttpMethod method, String path)
    {
        return permissions
                .stream()
                .filter(permission ->
                        permission.getMethod() == method && matcher.match(permission.getPath(), path))
                .findAny()
                .isPresent();
    }

    /**
     * Заполняет пермиссии пользователя из БД
     * @param login Логин пользователя
     */
    private void fillUserPermissions(String login)
    {
        if (login == null)
        {
            //throw new ServiceException(); или статус?
        }
    }
}
