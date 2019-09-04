package com.esb.oauthservice.resourcemanager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

/**
 * Description: Менеджер сообщений
 * @author AsMatveev
 */
@Component("resourceManager")
public class ResourceManager
{
    public static final String USER_NAME_NOT_SPECIFIED = "error.usernameNotSpecified";
    public static final String USER_NOT_FOUND = "error.userNotFound";
    public static final String FORBIDDEN_QUERY = "message.forbiddenQueryForUser";

    @Autowired
    private MessageSource resource;

    /**
     * Получить строку по ключу
     * @param key  Ключ
     * @param args Список аргументов
     * @return Сообщение
     */
    public String getResource(String key, Object... args)
    {
        return resource.getMessage(key, args, null);
    }
}
