package com.esb.oauthservice.ldap;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import java.util.Collection;
import java.util.Map;

public class EsbLdapUserDetails
        implements LdapUserDetails
{
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    private LdapUserDetails details;
    /**
     * Данные пользователя из LDAP
     */
    private Map<String, String> userInfo;

    public EsbLdapUserDetails(LdapUserDetails details)
    {
        this.details = details;
    }

    public Map<String, String> getUserInfo()
    {
        return userInfo;
    }

    public void setUserInfo(Map<String, String> userInfo)
    {
        this.userInfo = userInfo;
    }

    @Override
    public String getDn()
    {
        return details.getDn();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities()
    {
        return details.getAuthorities();
    }

    @Override
    public String getPassword()
    {
        return details.getPassword();
    }

    @Override
    public String getUsername()
    {
        return details.getUsername();
    }

    @Override
    public boolean isAccountNonExpired()
    {
        return details.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked()
    {
        return details.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired()
    {
        return details.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled()
    {
        return details.isEnabled();
    }
}
