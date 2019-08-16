package com.esb.oauthservice.ldap;

import com.esb.oauthservice.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.esb.oauthservice.ldap.LdapAttributesConst.ATTRIBUTES;

/**
 * Description: Извлекает информацию о пользователе из LDAP
 * @author AsMatveev
 */
public class EsbLdapUserDetailsContextMapper
        extends LdapUserDetailsMapper
{
    @Autowired
    private Logger logger;

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<?
            extends GrantedAuthority> authorities)
    {
        UserDetails userDetails = super.mapUserFromContext(ctx, username, authorities);
        EsbLdapUserDetails details = new EsbLdapUserDetails((LdapUserDetails) userDetails);

        Map<String, String> ldapAttributes = new HashMap<>();
        try
        {
            NamingEnumeration<String> iDs = ctx.getAttributes().getIDs();
            while (iDs.hasMore())
            {
                try
                {
                    String id = iDs.next();
                    ldapAttributes.put(id, String.valueOf(ctx.getAttributes().get(id).get()));
                }
                catch (Throwable ignored)
                {
                }
            }
        }
        catch (NamingException e)
        {
            logger.error("Ошибка получения LDAP атрибутов пользователя!", e);
        }
        logger.debug("READ LDAP ATTRIBUTES FOR USER: " + username);
        Map<String, String> userInfo = new HashMap<>();
        ldapAttributes.forEach((key, value) ->
        {
            // Для вывода всех атрибутов пользователя использовать: userInfo.put(key, value);
            if (ATTRIBUTES.containsKey(key))
            {
                userInfo.put(ATTRIBUTES.get(key), value);
            }
        });
        details.setUserInfo(userInfo);
        return details;
    }
}
