package com.esb.oauthservice.ldap;

import org.springframework.ldap.CommunicationException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;

import java.util.HashSet;

/**
 * ESBActiveDirectoryLdapAuthenticationProvider.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Провайдер LDAP аутентификации/авторизации
 */
public class ESBActiveDirectoryLdapAuthenticationProvider
        implements AuthenticationProvider
{
    private ActiveDirectoryLdapAuthenticationProvider provider;

    public ESBActiveDirectoryLdapAuthenticationProvider(ActiveDirectoryLdapAuthenticationProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException
    {
        try
        {
            Authentication authLdap = provider.authenticate(authentication);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(authLdap.getPrincipal(), authLdap.getCredentials(),
                            new HashSet<>());
            authenticationToken.setDetails(authLdap.getDetails());
            return authenticationToken;
        }
        catch (CommunicationException ex)
        {
            // в случае ошибки CommunicationException запишем её в лог и вернем проверку аутентификации следующему
            // провайдеру
            //logger.error("Ошибка подключения к LDAP серверу", ex);
            throw new LDAPCommunicationAuthenticationException();

        }
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
