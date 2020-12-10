package com.assd.oauthservice.userdetails;

import com.assd.oauthservice.storage.Permission;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Description: Данные пользователя из БД.
 * Используется как объект {@link UserDetails} для провайдера аутентификации пользователей из БД
 * @author AsMatveev
 */
public class AssdUser
        extends User
        implements AssdUserDetails
{
    private UUID userId;
    private List<Permission> permissions;

    public AssdUser(String username, String password, boolean enabled, boolean accountNonExpired,
                    boolean credentialsNonExpired, boolean accountNonLocked,
                    Collection<? extends GrantedAuthority> authorities, UUID userId, List<Permission> permissions)
    {
        super(username, password, enabled, accountNonExpired, credentialsNonExpired, accountNonLocked, authorities);
        this.permissions = permissions;
        this.userId = userId;
    }

    @Override
    public UUID getUserId()
    {
        return userId;
    }

    public void setUserId(UUID userId)
    {
        this.userId = userId;
    }

    @Override
    public String getName()
    {
        return super.getUsername();
    }

    @Override
    public List<String> getRoles()
    {
        return super.getAuthorities()
                    .stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toList());
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
}
