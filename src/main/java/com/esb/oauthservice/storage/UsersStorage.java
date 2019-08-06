package com.esb.oauthservice.storage;

import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

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
    // Идентификатор для LDAP пользователей
    public static final int LDAP_USER = -1;
    @Autowired
    private UsersDao usersDao;

    // Список всех аутентифицированных пользователей с их пермиссиями
    private Map<String, UserData> users = new HashMap<>();
    private AntPathMatcher matcher = new AntPathMatcher();

    /**
     * Возвращает id и список ролей пользователя в случае успешного доступа к запросу, иначе null
     * @param method Http метод запроса
     * @param path Путь запроса
     * @return Возвращает id и список ролей пользователя в случае успешного доступа к запросу, иначе null
     * @throws ServiceException
     */
    public UserResponseObject checkAccess(OAuth2Authentication auth, HttpMethod method, String path)
            throws ServiceException
    {
        String login = auth.getName();
        if (login == null||login.isEmpty())
        {
            //TODO Ошибка аутентификации. Имя пользователя не указано.
            throw new ServiceException();
        }
        if (!users.containsKey(login))
        {
            if (auth.getPrincipal() instanceof LdapUserDetails)
            {
                // Для LDAP пользователей пермиссии проверяются только для ролей, полученных из LDAP групп
                List<String> roles = auth.getAuthorities()
                                         .stream()
                                         .map(GrantedAuthority::getAuthority)
                                         .collect(Collectors.toList());
                fillUserPermissionsByRoles(login, roles);
            }
            else
            {
                fillUserPermissionsByLogin(login);
            }
        }
        UserData userData = users.get(login);
        if (checkAccess(userData.getPermissions(), method, path))
        {
            return userData.getUserResponseObject();
        }
        else
        {
            return null;
        }
    }

    /**
     * Проверка наличия доступа указанного запроса в списке пермиссий
     */
    private boolean checkAccess(List<Permission> permissions, HttpMethod method, String path)
    {
        return permissions.stream()
                          .filter(permission -> permission.getMethod() == method && matcher.match(permission.getPath(), path))
                          .findAny()
                          .isPresent();
    }

    /**
     * Заполняет пермиссии пользователя из БД
     * @param login Логин пользователя
     */
    private void fillUserPermissionsByLogin(String login)
    {
        users.put(login, UserData.builder()
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
    private void fillUserPermissionsByRoles(String login, List<String> roles)
    {
        users.put(login, UserData.builder()
                                 .permissions(usersDao.getPermissionsFromRoles(roles))
                                 .userResponseObject(UserResponseObject.builder()
                                                                       .id(LDAP_USER)
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
        users.remove(name);
    }
}
