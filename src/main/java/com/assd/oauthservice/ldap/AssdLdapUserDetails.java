package com.assd.oauthservice.ldap;

import com.assd.oauthservice.storage.Permission;
import com.assd.oauthservice.userdetails.AssdUserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.SpringSecurityCoreVersion;
import org.springframework.security.ldap.userdetails.LdapUserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Description: Обертка {@code LdapUserDetails} с дополнительными данными пользователя из LDAP
 * @author AsMatveev
 */
public class AssdLdapUserDetails
        implements LdapUserDetails, AssdUserDetails
{
    private static final long serialVersionUID = SpringSecurityCoreVersion.SERIAL_VERSION_UID;
    private LdapUserDetails details;
    private Integer userId;
    /**
     * Данные пользователя из LDAP. Формируются с помощью {@code LdapAttributesConst}
     */
    private Map<String, String> userInfo;

    /**
     * Пермиссии формируются по ролям пользователя, полученным после сопоставления LDAP групп и ролей
     */
    private List<Permission> permissions;

    public AssdLdapUserDetails(LdapUserDetails details)
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
    public List<Permission> getPermissions()
    {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions)
    {
        this.permissions = permissions;
    }

    @Override
    public Integer getUserId()
    {
        return userId;
    }

    public void setUserId(Integer userId)
    {
        this.userId = userId;
    }

    @Override
    public String getName()
    {
        return details.getUsername();
    }

    @Override
    public List<String> getRoles()
    {
        return details.getAuthorities()
                      .stream()
                      .map(GrantedAuthority::getAuthority)
                      .collect(Collectors.toList());
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
