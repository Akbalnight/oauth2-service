package com.assd.oauthservice.mongo;

import com.assd.oauthservice.userdetails.AssdUser;
import com.assd.oauthservice.ldap.AssdLdapUserDetails;
import com.assd.oauthservice.storage.Permission;
import org.bson.Document;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapUserDetailsImpl;

import java.util.*;

/**
 * Description: Конвертер для {@code UsernamePasswordAuthenticationToken}. Формирует объекты
 * {@code AssdLdapUserDetails} и {@code User} при чтении данных из mongodb.
 * @author AsMatveev
 */
@SuppressWarnings("unchecked")
public class UsernamePasswordAuthenticationTokenConverter
        implements Converter<Document, UsernamePasswordAuthenticationToken>
{
    @Override
    public UsernamePasswordAuthenticationToken convert(Document source)
    {
        Object principal = getPrincipalObject((Document) source.get("principal"));
        Object credentials = source.get("credentials");
        Collection<? extends GrantedAuthority> authorities = getAuthorities((List) source.get("authorities"));
        Object details = convertMap((Document) source.get("details"));

        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(principal, credentials, authorities);
        token.setDetails(details);
        if (!source.getBoolean("authenticated"))
            token.setAuthenticated(false);
        return token;
    }

    private Object getPrincipalObject(Document principal)
    {
        if (principal.get("_class").equals(AssdLdapUserDetails.class.getName()))
        {
            return convertAssdLdapUserDetails(principal);
        }
        else if (principal.get("_class").equals(AssdUser.class.getName()))
        {
            return convertAssdUser(principal);
        }
        else
        {
            throw new IllegalArgumentException("Ошибка парсинга UsernamePasswordAuthenticationToken");
        }
    }

    private AssdUser convertAssdUser(Document principal)
    {
        AssdUser user = new AssdUser(
                principal.getString("username"),
                "",
                principal.getBoolean("enabled"),
                principal.getBoolean("accountNonExpired"),
                principal.getBoolean("credentialsNonExpired"),
                principal.getBoolean("accountNonLocked"),
                getAuthorities((List) principal.get("authorities")),
                principal.getInteger("userId"),
                getPermissions((List) principal.get("permissions")));
        user.eraseCredentials();
        return user;
    }

    private List<Permission> getPermissions(List<Map<String, String>> permissions)
    {
        List<Permission> result = new ArrayList<>(permissions.size());
        permissions.forEach((permission) -> result.add(new Permission(HttpMethod.valueOf(permission.get("method")),
                permission.get("path"))));
        return result;
    }

    private AssdLdapUserDetails convertAssdLdapUserDetails(Document principal)
    {
        Document ldapDoc = (Document) principal.get("details");
        LdapUserDetailsImpl.Essence essence = new LdapUserDetailsImpl.Essence();
        essence.setUsername(ldapDoc.getString("username"));
        essence.setDn(ldapDoc.getString("dn"));
        essence.setAuthorities(getAuthorities((List) ldapDoc.get("authorities")));
        essence.setAccountNonExpired(ldapDoc.getBoolean("accountNonExpired"));
        essence.setAccountNonLocked(ldapDoc.getBoolean("accountNonLocked"));
        essence.setCredentialsNonExpired(ldapDoc.getBoolean("credentialsNonExpired"));
        essence.setEnabled(ldapDoc.getBoolean("enabled"));
        essence.setTimeBeforeExpiration(ldapDoc.getInteger("timeBeforeExpiration"));
        essence.setGraceLoginsRemaining(ldapDoc.getInteger("graceLoginsRemaining"));

        AssdLdapUserDetails assdLdapUserDetails = new AssdLdapUserDetails(essence.createUserDetails());
        assdLdapUserDetails.setUserInfo(convertMap((Document) principal.get("userInfo")));
        assdLdapUserDetails.setPermissions(getPermissions((List) principal.get("permissions")));
        return assdLdapUserDetails;
    }

    private Map<String, String> convertMap(Document documentMap)
    {
        Map<String, String> result = new LinkedHashMap<>();
        documentMap.forEach((key, value) -> result.put(key, String.valueOf(value)));
        return result;
    }

    private Collection<GrantedAuthority> getAuthorities(List<Map<String, String>> authorities)
    {
        Set<GrantedAuthority> grantedAuthorities = new HashSet<>(authorities.size());
        for (Map<String, String> authority : authorities)
        {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.get("role")));
        }
        return grantedAuthorities;
    }
}