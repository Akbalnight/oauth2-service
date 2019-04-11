package com.esb.oauthservice.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * GlobalExceptionsHadler.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Обработчик исключений при выполнении запросов
 */
@ControllerAdvice
@Component
public class GlobalExceptionsHadler
{
    @ExceptionHandler
    public ResponseEntity handleServiceException(ServiceException ex)
    {
        return new ResponseEntity(HttpStatus.BAD_REQUEST);
    }
}
