package com.esb.oauthservice.ldap;

import org.springframework.ldap.CommunicationException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.ldap.authentication.ad.ActiveDirectoryLdapAuthenticationProvider;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class ESBActiveDirectoryLdapAuthenticationProvider implements AuthenticationProvider
{
   // @Autowired
    //private Logger logger;
    //@Autowired
    //private AuthService authService;

    private ActiveDirectoryLdapAuthenticationProvider provider;
    public ESBActiveDirectoryLdapAuthenticationProvider(ActiveDirectoryLdapAuthenticationProvider provider)
    {
        this.provider = provider;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException
    {
        try
        {
            Authentication authLdap = provider.authenticate(authentication);
            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(authLdap.getPrincipal(), authLdap.getCredentials(),
                            mergeRoles(((LdapUserDetails) authLdap.getPrincipal()).getUsername(),
                                    authLdap.getAuthorities()));
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

    /**
     * Объединяет роли пользователя из БД (по логину) и соответствующие роли для LDAP групп пользователя
     * @param username логин пользователя
     * @param authorities группы пользователя из LDAP
     * @return возвращает список ролей пользователя
     */
    private Collection<? extends GrantedAuthority> mergeRoles(String username, Collection<? extends GrantedAuthority> authorities)
    {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();
        // Добавление ролей из БД по логину пользователя
    /*    List<String> roles =
                authService.getUserRoles(username).stream().map(Role::getName).collect(Collectors.toList());
        roles.forEach(role ->
        {
            mappedAuthorities.add(new SimpleGrantedAuthority(role));
        });

        // Добавление ролей соответствующих LDAP группам пользователя
        HashMap<String, String> rolesMap = authService.getLdapAuthoritiesMap();
        authorities.forEach(authority ->
        {
            rolesMap.keySet().forEach(ldapGroup ->
            {
                if (authority.getAuthority().toLowerCase().contains(ldapGroup.toLowerCase()))
                {
                    // Проверка что роль уже не добавлена пользователю
                    if (!roles.contains(rolesMap.get(ldapGroup)))
                    {
                        mappedAuthorities.add(new SimpleGrantedAuthority(rolesMap.get(ldapGroup)));
                    }
                    return;
                }
            });
        });*/

        return mappedAuthorities;
    }

    @Override
    public boolean supports(Class<?> authentication)
    {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
