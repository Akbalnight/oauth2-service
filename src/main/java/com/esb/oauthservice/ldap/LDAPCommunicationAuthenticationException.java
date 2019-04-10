package com.esb.oauthservice.ldap;

import org.springframework.security.core.AuthenticationException;

@SuppressWarnings("serial")
public class LDAPCommunicationAuthenticationException
        extends AuthenticationException
{
    public LDAPCommunicationAuthenticationException()
    {
        super("");
    }
}
