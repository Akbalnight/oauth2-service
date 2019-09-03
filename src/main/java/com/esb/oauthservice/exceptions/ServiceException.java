package com.esb.oauthservice.exceptions;

import com.esb.oauthservice.dto.ExceptionResponseObject;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

/**
 * Description: Класс для обертки и генерации исключений сервиса при выполнении запросов
 * @author AsMatveev
 */
public class ServiceException
        extends RuntimeException
{
    private final HttpStatus status;
    private final String errorMessage;

    public ServiceException(HttpStatus httpStatus, String errorMessage)
    {
        this.status = httpStatus;
        this.errorMessage = errorMessage;
    }

    /**
     * Возвращает {@code ResponseEntity} сформированный на основе данных исключния
     */
    public ResponseEntity getResponse()
    {
        return ResponseEntity.status(status)
                             .contentType(MediaType.APPLICATION_JSON)
                             .body(getErrorResponse());
    }

    @Override
    public String getMessage()
    {
        return errorMessage;
    }

    private ExceptionResponseObject getErrorResponse()
    {
        return ExceptionResponseObject.builder()
                                      .status(status.value())
                                      .error_description(errorMessage)
                                      .build();
    }
}
