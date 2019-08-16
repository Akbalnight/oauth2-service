package com.esb.oauthservice.storage;

import com.esb.oauthservice.exceptions.ServiceException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;

import java.util.List;

/**
 * Description: Хранилище аутентифицированных пользователей и их данных
 * @author AsMatveev
 */
public interface UsersStorage
{
    /**
     * Возращает данные авторизованного пользователя
     * @param authentication Данные аутентификации пользователя
     * @return Возращает данные пользователя
     * @throws ServiceException Исключение если пользователь не найден
     */
    UserData getUser(OAuth2Authentication authentication)
            throws ServiceException;

    /**
     * Возвращает список пермиссий авторизованного пользоватлея
     * @param authentication Данные аутентификации пользователя
     * @return Возвращает список пермиссий
     * @throws ServiceException Исключение если пользователь не найден
     */
    List<Permission> getUserPermissions(OAuth2Authentication authentication)
            throws ServiceException;

    /**
     * Добавляет и авторизует пользователя в хранилище
     * @param authentication Данные аутентификации пользователя
     */
    void addUser(OAuth2Authentication authentication);

    /**
     * Проверка что пользователь существует в хранилище
     * @param authentication Данные аутентификации пользователя
     * @return Возвращет true если пользователь найден в хранилище
     */
    boolean isUserExist(OAuth2Authentication authentication);

    /**
     * Удаляет пользователя из хранилища
     * @param authentication Данные аутентификации пользователя
     */
    void removeUser(OAuth2Authentication authentication);
}
