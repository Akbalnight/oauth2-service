package com.esb.oauthservice.database;

import com.esb.oauthservice.storage.Permission;

import java.util.List;
import java.util.Map;

/**
 * @author amatveev
 * Description: Интерфейс для работы с БД пользователей
 */
public interface UsersDao
{
    /**
     * Возвращает сопоставления LDAP групп c ролями пользователей из БД
     */
    Map<String, String> getLdapAuthoritiesMap();

    /**
     * Возвращает все пермиссии пользователя
     * @param login Логин пользователя
     */
    List<Permission> getUserPermissions(String login);

    /**
     * Возвращает id пользователя
     * @param login Логин пользователя
     */
    Integer getUserId(String login);

    /**
     * Возвращает роли пользователя
     * @param login Логин пользователя
     */
    List<String> getUserRoles(String login);

    /**
     * Возвращает набор пермиссий указанных ролей
     * @param roles Список ролей
     */
    List<Permission> getPermissionsFromRoles(List<String> roles);
}
