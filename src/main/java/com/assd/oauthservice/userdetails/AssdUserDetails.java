package com.assd.oauthservice.userdetails;

import com.assd.oauthservice.storage.Permission;

import java.util.List;
import java.util.UUID;

/**
 * Description: Интерфейс для получения данных аутентификации пользователей
 * @author AsMatveev
 */
public interface AssdUserDetails
{
    /**
     * Идентификатор пользователя.
     * Для LDAP пользоватлей null
     * @return Возвращает id пользователя
     */
    UUID getUserId();

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
