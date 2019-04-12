package com.esb.oauthservice.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ExceptionResponseObject.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Класс для описания исключения возвращаемого пользователю
 */
@Getter
@Setter
@Builder
public class ExceptionResponseObject
{
    private String error;
    private String error_description;
}
