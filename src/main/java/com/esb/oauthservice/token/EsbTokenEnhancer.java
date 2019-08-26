package com.esb.oauthservice.token;

import com.esb.oauthservice.userdetails.EsbUserDetails;
import com.esb.oauthservice.database.UsersDao;
import com.esb.oauthservice.ldap.EsbLdapUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
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
    private UsersDao usersDao;

    /**
     * Добавление в токен информации о пользователе
     */
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication)
    {
        final Map<String, Object> additionalInfo = new HashMap<>();
        if (authentication.getPrincipal() instanceof EsbUserDetails)
        {
            additionalInfo.putAll(getUserInfo((EsbUserDetails) authentication.getPrincipal()));
            if (authentication.getPrincipal() instanceof EsbLdapUserDetails)
            {
                // Для LDAP пользователей сохраняются данные из LDAP
                additionalInfo.putAll(getLdapInfo((EsbLdapUserDetails) authentication.getPrincipal()));
            }
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }

    /**
     * Формирует данные пользователя, полученные из LDAP
     * @param principal Данные пользователя
     */
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

    /**
     * Формирует список пермиссиий, логин, id и список ролей пользователя
     * @param userDetails Данные пользователя
     */
    private Map<String, Object> getUserInfo(EsbUserDetails userDetails)
    {
        Map<String, Object> info = new HashMap<>();
        info.put(USER_NAME, userDetails.getName());
        info.put(ROLES, userDetails.getRoles()
                                      .stream().collect(Collectors.joining(", ", "[", "]")));
        info.put(PERMISSIONS, userDetails.getPermissions());
        if (userDetails.getUserId() != null)
        {
            info.put(USER_ID, userDetails.getUserId());
        }
        return info;
    }
}