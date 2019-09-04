package com.esb.oauthservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Description: Исключение сервиса со статусом 403 и параметрами запроса и пользователя
 * @author AsMatveev
 */
public class ForbiddenQueryException
        extends ServiceException
{
    /**
     * @param message Сообщение с данными пользователя и запроса
     */
    public ForbiddenQueryException(String message)
    {
        super(HttpStatus.FORBIDDEN, message);
    }
}
