package com.esb.oauthservice.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
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
