package com.esb.oauthservice.ldap;

import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author amatveev
 * Description: {@code GrantedAuthoritiesMapper} для замены LDAP групп пользователя на роли из БД
 */
public class LdapAuthoritiesMapper
        implements GrantedAuthoritiesMapper
{
    @Autowired
    private UsersDao usersDao;
    @Autowired
    private Logger logger;

    /**
     * Преобразует группы пользователя из LDAP в роли из БД.
     * @param userGroups Группы пользователя из LDAP
     * @return Возвращает список ролей пользователя
     */
    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> userGroups)
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
