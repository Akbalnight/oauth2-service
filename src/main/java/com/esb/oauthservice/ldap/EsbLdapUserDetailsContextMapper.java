package com.esb.oauthservice.ldap;

import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import java.util.*;

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
    @Autowired
    private UsersDao usersDao;

    private final String ldapDomen;

    public EsbLdapUserDetailsContextMapper(String ldapDomen)
    {
        this.ldapDomen = ldapDomen;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<?
            extends GrantedAuthority> authorities)
    {
        username = prepareUsername(username);
        UserDetails userDetails = super.mapUserFromContext(ctx, username, mapAuthorities(authorities));
        EsbLdapUserDetails details = new EsbLdapUserDetails((LdapUserDetails) userDetails);

        details.setUserInfo(getUserInfo(ctx));
        logger.debug("READ LDAP ATTRIBUTES FOR USER: " + username);
        details.setPermissions(usersDao.getPermissionsFromRoles(details.getRoles()));

        return details;
    }

    private String prepareUsername(String login)
    {
        login = login.toLowerCase();
        // Добавим домен к логину пользователя если он не указан
        if (!login.endsWith("@" + ldapDomen))
        {
            login = login + "@" + ldapDomen;
        }
        return login;
    }

    /**
     * Считывает данные пользователя из LDAP
     */
    private Map<String, String> getUserInfo(DirContextOperations ctx)
    {
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
        Map<String, String> userInfo = new HashMap<>();
        ldapAttributes.forEach((key, value) ->
        {
            // Для вывода всех атрибутов пользователя использовать: userInfo.put(key, value);
            if (ATTRIBUTES.containsKey(key))
            {
                userInfo.put(ATTRIBUTES.get(key), value);
            }
        });
        return userInfo;
    }

    /**
     * Преобразует группы пользователя из LDAP в роли из БД
     * @param userGroups Группы пользователя из LDAP
     * @return Возвращает список ролей пользователя
     */
    private Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> userGroups)
    {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        // Добавление ролей соответствующих LDAP группам пользователя
        Map<String, String> rolesMap = usersDao.getLdapAuthoritiesMap();
        userGroups.forEach(authority -> rolesMap.keySet()
                                                .forEach(ldapGroup ->
                                                {
                                                    if (authority.getAuthority()
                                                                 .toLowerCase()
                                                                 .contains(ldapGroup.toLowerCase()))
                                                    {
                                                        mappedAuthorities.add(new SimpleGrantedAuthority(rolesMap.get(ldapGroup)));
                                                    }
                                                }));
        return mappedAuthorities;
    }
}
