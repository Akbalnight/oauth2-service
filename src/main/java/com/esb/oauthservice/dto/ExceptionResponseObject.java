package com.esb.oauthservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * ExceptionResponseObject.java
 * Date: 10 апр. 2019 г.
 * @author AsMatveev
 * Description: DTO исключений
 */
@Getter
@Setter
@Builder
public class ExceptionResponseObject
{
    private Integer status;
    private String error_description;
}
