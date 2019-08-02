package com.esb.oauthservice.storage;

import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.exceptions.ServiceException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, UserData> users = new HashMap<>();
    private AntPathMatcher matcher = new AntPathMatcher();


    /**
     * Возвращает id и список ролей пользователя в случае успешного доступа к запросу, иначе null
     * @param login Логин пользователя
     * @param method Http метод запроса
     * @param path Путь запроса
     * @return Возвращает id и список ролей пользователя в случае успешного доступа к запросу, иначе null
     * @throws ServiceException
     */
    public UserResponseObject checkAccess(String login, HttpMethod method, String path)
            throws ServiceException
    {
        if (!users.containsKey(login))
        {
            fillUserPermissions(login);
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
        return permissions
                .stream()
                .filter(permission -> permission.getMethod() == method && matcher.match(permission.getPath(), path))
                .findAny()
                .isPresent();
    }

    /**
     * Заполняет пермиссии пользователя из БД
     * @param login Логин пользователя
     */
    private void fillUserPermissions(String login)
            throws ServiceException
    {
        if (login == null)
        {
            throw new ServiceException();
        }
        users.put(login, UserData
                .builder()
                .permissions(usersDao.getUserPermissions(login))
                .userResponseObject(UserResponseObject
                        .builder()
                        .id(usersDao.getUserId(login))
                        .roles(usersDao.getUserRoles(login))
                        .build())
                .build());
    }

    /**
     * Удаление пользователея из хранилища
     * @param name Логин пользователя
     */
    public void removeUser(String name)
    {
        users.remove(name);
    }
}
