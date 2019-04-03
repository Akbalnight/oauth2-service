package com.esb.oauthservice.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.http.HttpMethod;

@Getter
@Setter
public class Permission
{
    private HttpMethod method;
    private String path;
}
