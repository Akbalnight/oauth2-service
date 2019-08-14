package com.esb.oauthservice.storage;

import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.ldap.EsbLdapUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UsersStorage.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Хранилище аутентифицированных пользователей и их данных
 */
@Component
public class UsersStorage
{
    @Autowired
    private UsersDao usersDao;

    // Список всех аутентифицированных пользователей с их пермиссиями
    private Map<String, UserData> usersDb = new HashMap<>();
    private Map<String, UserData> usersLdap = new HashMap<>();

    public UserData getUser(OAuth2Authentication authentication)
    {
        String login = authentication.getName();
        if (isLdapUser(authentication))
        {
            if (!usersLdap.containsKey(login))
            {
                throw new UsernameNotFoundException(login);
            }
            return usersLdap.get(login);

        }
        if (isDatabaseUser(authentication))
        {
            if (!usersDb.containsKey(login))
            {
                throw new UsernameNotFoundException(login);
            }
            return usersDb.get(login);
        }
        throw new UsernameNotFoundException(login);
    }

    private boolean isDatabaseUser(OAuth2Authentication authentication)
    {
        return authentication.getPrincipal() instanceof User;
    }

    private boolean isLdapUser(OAuth2Authentication authentication)
    {
        return authentication.getPrincipal() instanceof EsbLdapUserDetails;
    }

    /**
     * Заполняет пермиссии пользователя из БД
     * @param login Логин пользователя
     */
    private void fillDbUserPermissionsByLogin(String login)
    {
        usersDb.put(login, UserData.builder()
                                   .permissions(usersDao.getUserPermissions(login))
                                   .userResponseObject(UserResponseObject.builder()
                                                                         .id(usersDao.getUserId(login))
                                                                         .roles(usersDao.getUserRoles(login))
                                                                         .build())
                                   .build());
    }

    /**
     * Заполняет пермиссии по списку ролей
     * @param login Логин пользователя
     * @param roles Список ролей
     */
    private void fillLdapUserPermissionsByRoles(String login, List<String> roles)
    {
        usersLdap.put(login, UserData.builder()
                                     .permissions(usersDao.getPermissionsFromRoles(roles))
                                     .userResponseObject(UserResponseObject.builder()
                                                                           .roles(roles)
                                                                           .build())
                                     .build());
    }

    /**
     * Удаление пользователя из хранилища
     * @param name Логин пользователя
     */
    public void removeUser(String name)
    {
        usersDb.remove(name);
    }

    public List<Permission> getUserPermissions(OAuth2Authentication authentication)
    {
        return getUser(authentication).getPermissions();
    }

    public void checkOrFillUserPermissions(OAuth2Authentication authentication)
    {
        String username = authentication.getName();
        if (isLdapUser(authentication) && !usersLdap.containsKey(username))
        { // Для LDAP пользователей пермиссии проверяются только для ролей, полученных из LDAP групп
            List<String> roles = authentication.getAuthorities()
                                               .stream()
                                               .map(GrantedAuthority::getAuthority)
                                               .collect(Collectors.toList());
            fillLdapUserPermissionsByRoles(username, roles);
        }
        if (isDatabaseUser(authentication) && !usersDb.containsKey(username))
        {
            fillDbUserPermissionsByLogin(username);
        }
    }
}
