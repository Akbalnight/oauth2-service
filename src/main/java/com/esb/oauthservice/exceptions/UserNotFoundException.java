package com.esb.oauthservice.exceptions;

import org.springframework.http.HttpStatus;

/**
 * Description: Исключение со статусом 404 и логином пользователя
 * @author AsMatveev
 */
public class UserNotFoundException
        extends ServiceException
{
    public UserNotFoundException(String login)
    {
        super(HttpStatus.NOT_FOUND, String.format("Пользователь \'%s\' не найден", login));
    }
}