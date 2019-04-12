package com.esb.oauthservice.ldap;

import org.springframework.security.core.AuthenticationException;

/**
 * LDAPCommunicationAuthenticationException.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Исключение при ошибке доступности LDAP сервера
 */
@SuppressWarnings("serial")
public class LDAPCommunicationAuthenticationException
        extends AuthenticationException
{
    public LDAPCommunicationAuthenticationException()
    {
        super("");
    }
}