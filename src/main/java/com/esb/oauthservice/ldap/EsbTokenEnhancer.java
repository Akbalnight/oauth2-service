package com.esb.oauthservice.ldap;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class EsbTokenEnhancer
        implements TokenEnhancer
{
    @Override
    public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication)
    {
        final Map<String, Object> additionalInfo = new HashMap<>();
        if (authentication.getPrincipal() instanceof UserDetails)
        {
            UserDetails principal = (UserDetails) authentication.getPrincipal();
            additionalInfo.put("username", principal.getUsername());
            additionalInfo.put("roles", principal.getAuthorities()
                                                 .stream()
                                                 .map(GrantedAuthority::getAuthority)
                                                 .collect(Collectors.joining(", ", "[", "]")));

            if (authentication.getPrincipal() instanceof EsbLdapUserDetails)
            {
                Map<String, String> userInfo = ((EsbLdapUserDetails) authentication.getPrincipal()).getUserInfo();
                userInfo.entrySet()
                        .stream()
                        .filter(entry -> entry.getValue() != null)
                        .forEach(entry -> additionalInfo.put(entry.getKey(), entry.getValue()));
            }
        }
        ((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
        return accessToken;
    }
}