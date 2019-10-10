package com.assd.oauthservice.database;

import com.assd.oauthservice.storage.Permission;

import java.util.List;
import java.util.Map;
import java.util.Set;

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

    /**
     * Добавляет пользователя из LDAP в БД
     * @param username Логин пользователя
     * @param userInfo Данные пользователя из LDAP
     * @return Возвращает id добавленного пользователя
     */
    Integer addUserFromLdap(String username, Map<String, String> userInfo);

    /**
     * Обновляет роли пользователя в БД
     * @param username Логин пользователя
     * @param roles Список ролей пользователя
     */
    void updateUserRoles(String username, Set<String> roles);

    /**
     * Возвращает роли пользователя, у которых нет метки LDAP
     * @param username Логин пользователя
     * @return Возвращает список ролей
     */
    List<String> getNotLdapUserRoles(String username);
}
