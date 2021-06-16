package com.assd.oauthservice.ldap;

import com.assd.oauthservice.database.UsersDao;
import com.assd.oauthservice.exceptions.UserNotFoundException;
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
import java.util.stream.Collectors;

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
        final Collection<? extends GrantedAuthority> roles = mapAuthorities(username, authorities);
        UserDetails userDetails = super.mapUserFromContext(ctx, username, roles);
        AssdLdapUserDetails details = new AssdLdapUserDetails((LdapUserDetails) userDetails);
        final Map<String, String> userInfo = getUserInfo(ctx);
        details.setUserInfo(userInfo);

        UUID userId = findUserId(username);
        if (userId == null)
        {
            // После аутентификации через LDAP добавим пользователя в БД
            userId = usersDao.addUserFromLdap(username, userInfo);
//            userId = usersDao.addUserFromLdap(username, null);

        }
        // Перезапишем роли пользователя, полученные из LDAP групп в БД
        usersDao.updateUserRoles(username, roles.stream()
                                             .map(GrantedAuthority::getAuthority)
                                             .collect(Collectors.toSet()));
        details.setUserId(userId);
        logger.debug("READ LDAP ATTRIBUTES FOR USER: " + username);
        details.setPermissions(usersDao.getPermissionsFromRoles(details.getRoles()));

        return details;
    }

    /**
     * Возвращает id пользователя из БД по логину
     * @param username Логин пользователя
     * @return Возвращает id пользователя
     */
    private UUID findUserId(String username)
    {
        try
        {
            return usersDao.getUserId(username);
        }
        catch (UserNotFoundException ex)
        {
            return null;
        }
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
        logger.debug("LDAP ATTRIBUTES: ");
        ldapAttributes.forEach((key, value) ->
        {
            logger.debug("ATTRIBUTE: " + key + " : " + value);
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
    private Collection<? extends GrantedAuthority> mapAuthorities(String username, Collection<?
            extends GrantedAuthority> userGroups)
    {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        // Добавление ролей из БД по логину пользователя, которые не были назначены через LDAP
        List<String> roles = usersDao.getNotLdapUserRoles(username);
        roles.forEach(role -> mappedAuthorities.add(new SimpleGrantedAuthority(role)));

//        for(Map.Entry<String, DatabaseSettings> database : readDataBases.entrySet()){

        logger.debug("LDAP пользователь " + username + ". Группы: " + userGroups);
        // Добавление ролей соответствующих LDAP группам пользователя
        Map<String, String> rolesMap = usersDao.getLdapAuthoritiesMap();
        userGroups.forEach(authority -> rolesMap.keySet().forEach(ldapGroup ->
                {
                    if (authority.getAuthority().toLowerCase().contains(ldapGroup.toLowerCase()))
                    {
                        // Проверка что роль уже не добавлена пользователю
                        if (!roles.contains(rolesMap.get(ldapGroup)))
                        {
                            mappedAuthorities.add(new SimpleGrantedAuthority(rolesMap.get(ldapGroup)));
                        }
                    }
                }));
        logger.debug("LDAP пользователь " + username + ". Роли: " + mappedAuthorities);
        return mappedAuthorities;
    }
}
