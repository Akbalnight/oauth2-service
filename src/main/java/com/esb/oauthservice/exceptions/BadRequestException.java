package com.esb.oauthservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Description: Исключение сервиса со статусом 400 и текстом ошибки
 * @author AsMatveev
 */
public class BadRequestException
        extends ServiceException
{
    public BadRequestException(String errorMessage)
    {
        super(HttpStatus.BAD_REQUEST, errorMessage);
    }
}
