package com.esb.oauthservice.ldap;

import com.esb.oauthservice.database.UsersDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.authority.mapping.GrantedAuthoritiesMapper;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class LdapAuthoritiesMapper
        implements GrantedAuthoritiesMapper
{
    @Autowired
    private UsersDao usersDao;

    @Override
    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> userGroups)
    {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        // Добавление ролей соответствующих LDAP группам пользователя
        Map<String, String> rolesMap = usersDao.getLdapAuthoritiesMap();
        userGroups.forEach(userGroup ->
        {
            if (rolesMap.containsKey(userGroup))
            {
                mappedAuthorities.add(new SimpleGrantedAuthority(rolesMap.get(userGroup)));
            }
        });
        return mappedAuthorities;
    }
}
