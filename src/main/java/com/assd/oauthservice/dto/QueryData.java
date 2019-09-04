package com.assd.oauthservice.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

/**
 * QueryData.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Описание данных запроса
 */
@Getter
@Setter
@Builder
public class QueryData
{
    /**
     * URL путь
     */
    private String path;
    /**
     * HTTP Метод
     */
    private HttpMethod method;
}
