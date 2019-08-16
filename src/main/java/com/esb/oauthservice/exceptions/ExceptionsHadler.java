package com.esb.oauthservice.exceptions;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * GlobalExceptionsHadler.java
 * Date: 10 апр. 2019 г.
 * @author AsMatveev
 * Description: Обработчик исключений сервиса
 */
@ControllerAdvice
@Component
public class ExceptionsHadler
{
    @ExceptionHandler(ServiceException.class)
    public ResponseEntity handleBaseServiceException(ServiceException ex)
    {
        return ex.getResponse();
    }
}
