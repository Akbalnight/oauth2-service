package com.assd.oauthservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ExceptionResponseObject.java
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Класс для передачи данных исключении
 */
@Getter
@Setter
@Builder
public class ExceptionResponseObject
{
    /**
     * Http статус ошибки
     */
    private Integer status;

    /**
     * Код ошибки
     */
    private String error;

    /**
     * Описание ошибки
     */
    private String error_description;
}
