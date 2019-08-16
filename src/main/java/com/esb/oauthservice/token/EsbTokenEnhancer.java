package com.esb.oauthservice.token;

import com.esb.oauthservice.ldap.EsbLdapUserDetails;
import com.esb.oauthservice.storage.UserData;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.esb.oauthservice.storage.AdditionalInformationConst.*;

/**
 * @author AsMatveev
 * Description: Добавление в токен информации о пользователе перед сохранением токена в {@code TokenStore}
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
            // При создании токена добавим/обновим пользователя в хранилище
            usersStorage.addUser(authentication);
            additionalInfo.putAll(getUserInfo(authentication));

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

    private Map<String, Object> getUserInfo(OAuth2Authentication authentication)
    {
        Map<String, Object> info = new HashMap<>();
        UserData userData = usersStorage.getUser(authentication);
        String username = ((UserDetails) authentication.getPrincipal()).getUsername();
        info.put(USER_NAME, username);
        info.put(ROLES, authentication.getAuthorities()
                                      .stream()
                                      .map(GrantedAuthority::getAuthority)
                                      .collect(Collectors.joining(", ", "[", "]")));
        info.put(PERMISSIONS, userData.getPermissions());
        if (userData.getUserResponseObject().getId() != null)
        {
            info.put(USER_ID, userData.getUserResponseObject().getId());
        }
        return info;
    }
}