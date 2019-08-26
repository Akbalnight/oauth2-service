package com.esb.oauthservice.userdetails;

import com.esb.oauthservice.storage.Permission;

import java.util.List;

/**
 * Description: Интерфейс для получения данных аутентификации пользователей
 * @author AsMatveev
 */
public interface EsbUserDetails
{
    /**
     * Идентификатор пользователя.
     * Для LDAP пользоватлей null
     * @return Возвращает id пользователя
     */
    Integer getUserId();

    /**
     * @return Возвращает логин пользователя
     */
    String getName();

    /**
     * @return Возвращает список ролей пользователя
     */
    List<String> getRoles();

    /**
     * @return Возвращает список пермиссий пользователя
     */
    List<Permission> getPermissions();
}
