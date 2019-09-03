package com.esb.oauthservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ExceptionResponseObject.java
 * Date: 10 апр. 2019 г.
 * @author AsMatveev
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
     * Описание ошибки
     */
    private String error_description;
}
