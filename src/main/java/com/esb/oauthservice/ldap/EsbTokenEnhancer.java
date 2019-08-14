package com.esb.oauthservice.ldap;

import com.esb.oauthservice.storage.Permission;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author amatveev
 * Description: Добавление в токен информации о пользователе
 */
public class EsbTokenEnhancer
        implements TokenEnhancer
{
    @Autowired
    private UsersStorage usersStorage;
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication)
    {
        final Map<String, Object> additionalInfo = new HashMap<>();
        if (authentication.getPrincipal() instanceof UserDetails)
        {
            usersStorage.checkOrFillUserPermissions(authentication);
            additionalInfo.putAll(findUserData(authentication));

            if (authentication.getPrincipal() instanceof EsbLdapUserDetails)
            {
                // Для LDAP пользователей сохраняются данные из LDAP
                additionalInfo.putAll(getLdapInfo((EsbLdapUserDetails) authentication.getPrincipal()));
            }
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    private Map<String, Object> getLdapInfo(EsbLdapUserDetails principal)
    {
        Map<String, Object> ldapInfo = new HashMap<>();
        principal.getUserInfo()
                 .entrySet()
                 .stream()
                 .filter(entry -> entry.getValue() != null)
                 .forEach(entry -> ldapInfo.put(entry.getKey(), entry.getValue()));
        return ldapInfo;
    }

    private Map<String, Object> findUserData(OAuth2Authentication authentication)
    {
        Map<String, Object> userData = new HashMap<>();
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        userData.put("username", username);
        userData.put("roles", authentication.getAuthorities()
                                            .stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .collect(Collectors.joining(", ", "[", "]")));
        userData.put("permissions", getUserPermissions(authentication));
        return userData;
    }

    private List<Permission> getUserPermissions(OAuth2Authentication authentication)
    {
       return usersStorage.getUserPermissions(authentication);
    }
}