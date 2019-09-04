package com.assd.oauthservice.ldap;

import com.assd.oauthservice.database.UsersDao;
import com.assd.oauthservice.logger.Logger;
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

/**
 * Description: Извлекает информацию о пользователе из LDAP
 * @author AsMatveev
 */
public class AssdLdapUserDetailsContextMapper
        extends LdapUserDetailsMapper
{
    @Autowired
    private Logger logger;
    @Autowired
    private UsersDao usersDao;

    private final String ldapDomen;

    /**
     * Конструктор
     * @param ldapDomen Домен LDAP сервера
     */
    public AssdLdapUserDetailsContextMapper(String ldapDomen)
    {
        this.ldapDomen = ldapDomen;
    }

    @Override
    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<?
            extends GrantedAuthority> authorities)
    {
        username = prepareUsername(username);
        UserDetails userDetails = super.mapUserFromContext(ctx, username, mapAuthorities(authorities));
        AssdLdapUserDetails details = new AssdLdapUserDetails((LdapUserDetails) userDetails);

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
            if (LdapAttributesConst.ATTRIBUTES.containsKey(key))
            {
                userInfo.put(LdapAttributesConst.ATTRIBUTES.get(key), value);
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
