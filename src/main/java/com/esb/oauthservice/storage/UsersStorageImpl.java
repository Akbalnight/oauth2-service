package com.esb.oauthservice.storage;

import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.dto.UserResponseObject;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.exceptions.UserNotFoundException;
import com.esb.oauthservice.ldap.EsbLdapUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
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
public class UsersStorageImpl
        implements UsersStorage
{
    @Autowired
    private UsersDao usersDao;

    // Список всех аутентифицированных пользователей с их пермиссиями
    private final Map<String, UserData> usersDb = new HashMap<>();
    private final Map<String, UserData> usersLdap = new HashMap<>();

    @Override
    public UserData getUser(OAuth2Authentication authentication)
            throws ServiceException
    {
        String login = authentication.getName();
        if (isLdapUser(authentication) && usersLdap.containsKey(login))
        {
            return usersLdap.get(login);
        }
        else if (isDatabaseUser(authentication) && usersDb.containsKey(login))
        {
            return usersDb.get(login);
        }
        throw new UserNotFoundException(login);
    }

    @Override
    public List<Permission> getUserPermissions(OAuth2Authentication authentication)
            throws ServiceException
    {
        return getUser(authentication).getPermissions();
    }

    @Override
    public void addUser(OAuth2Authentication authentication)
    {
        String username = authentication.getName();
        if (isLdapUser(authentication))
        { // Для LDAP пользователей пермиссии проверяются только для ролей, полученных из LDAP групп
            List<String> roles = authentication.getAuthorities()
                                               .stream()
                                               .map(GrantedAuthority::getAuthority)
                                               .collect(Collectors.toList());
            fillLdapUserPermissionsByRoles(username, roles);
        }
        else if (isDatabaseUser(authentication))
        {
            fillDbUserPermissionsByLogin(username);
        }
    }

    @Override
    public boolean isUserExist(OAuth2Authentication authentication)
    {
        String username = authentication.getName();
        if (isLdapUser(authentication) && usersLdap.containsKey(username))
        {
            return true;
        }
        if (isDatabaseUser(authentication) && usersDb.containsKey(username))
        {
            return true;
        }
        return false;
    }

    @Override
    public void removeUser(OAuth2Authentication authentication)
    {
        String username = authentication.getName();
        if (isLdapUser(authentication))
        {
            usersLdap.remove(username);
        }
        if (isDatabaseUser(authentication))
        {
            usersDb.remove(username);
        }
    }

    @Override
    public void removeUser(String username, boolean isLdapUser)
    {
        if (isLdapUser)
        {
            usersLdap.remove(username);
        }
        else
        {
            usersDb.remove(username);
        }
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

    private boolean isDatabaseUser(OAuth2Authentication authentication)
    {
        return authentication.getPrincipal() instanceof User;
    }

    private boolean isLdapUser(OAuth2Authentication authentication)
    {
        return authentication.getPrincipal() instanceof EsbLdapUserDetails;
    }
}
