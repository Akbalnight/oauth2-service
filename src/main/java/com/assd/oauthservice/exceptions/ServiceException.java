package com.assd.oauthservice.exceptions;

import com.assd.oauthservice.dto.ExceptionResponseObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Класс для обертки и генерации исключений сервиса при выполнении запросов
 */
@Log4j2
public class ServiceException
        extends RuntimeException
{
    private final HttpStatus status;
    private final String errorCode;
    private final String errorMessage;

    public ServiceException(HttpStatus httpStatus, String errorMessage)
    {
        this(httpStatus, httpStatus.name(), errorMessage);
    }

    public ServiceException(HttpStatus httpStatus, String errorCode, String errorMessage)
    {
        log.warn("ServiceException => status [{}]", httpStatus);
        log.warn("ServiceException => errorCode [{}]", errorCode);
        log.warn("ServiceException => errorMessage [{}]", errorMessage);
        this.status = httpStatus;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
    }

    /**
     * Возвращает {@code ResponseEntity} сформированный на основе данных исключния
     */
    public ResponseEntity getResponse()
    {
        log.warn("getResponse => status [{}]", status);
        log.warn("getResponse => errorMessage [{}]", errorMessage);
        return ResponseEntity.status(status)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(getErrorResponse());
    }

    @Override
    public String getMessage()
    {
        log.warn("getResponse => status [{}]", status);
        log.warn("getResponse => errorMessage [{}]", errorMessage);
        return errorMessage;
    }

    private ExceptionResponseObject getErrorResponse()
    {
        return ExceptionResponseObject.builder()
                .status(status.value())
                .error(errorCode)
                .error_description(errorMessage)
                .build();
    }
}
