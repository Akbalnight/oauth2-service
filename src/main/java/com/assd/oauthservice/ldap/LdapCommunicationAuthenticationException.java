package com.assd.oauthservice.ldap;

import org.springframework.security.core.AuthenticationException;

/**
 * LDAPCommunicationAuthenticationException.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Исключение при ошибке доступности LDAP сервера
 */
@SuppressWarnings("serial")
public class LdapCommunicationAuthenticationException
        extends AuthenticationException
{
    public LdapCommunicationAuthenticationException()
    {
        super("");
    }
}