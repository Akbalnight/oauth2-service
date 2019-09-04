package com.esb.oauthservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Description: Исключение со статусом 404 и логином пользователя
 * @author AsMatveev
 */
public class UserNotFoundException
        extends ServiceException
{
    public UserNotFoundException(String message)
    {
        super(HttpStatus.NOT_FOUND, message);
    }
}