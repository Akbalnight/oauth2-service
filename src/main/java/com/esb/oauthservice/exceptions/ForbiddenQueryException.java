package com.esb.oauthservice.exceptions;

import com.esb.oauthservice.dto.QueryData;
import org.springframework.http.HttpStatus;

/**
 * Description: Исключение сервиса со статусом 403 и параметрами запроса и пользователя
 * @author AsMatveev
 */
public class ForbiddenQueryException
        extends ServiceException
{
    /**
     * @param name      Логин пользователя
     * @param queryData Данные запроса
     */
    public ForbiddenQueryException(String name, QueryData queryData)
    {
        super(HttpStatus.FORBIDDEN, String.format("У пользователя '%s' нет доступа к методу %s '%s'", name,
                queryData.getMethod(), queryData.getPath()));
    }
}
