package com.esb.oauthservice.token;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.core.AuthenticationException;

import java.util.Date;

/**
 * @author AsMatveev
 * Description: Переопределяет {@code DefaultTokenServices}
 */
public class EsbTokenService
        extends DefaultTokenServices
{
    @Autowired
    private TokenStore tokenStore;

    /**
     * Обновляет время жизни accessToken при каждом запросе аутентификации
     */
    @Override
    @Transactional
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication)
            throws AuthenticationException
    {
        OAuth2AccessToken existingAccessToken = tokenStore.getAccessToken(authentication);
        if (existingAccessToken != null)
        {
            if (!existingAccessToken.isExpired())
            {
                DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) existingAccessToken;

                int validitySeconds = getAccessTokenValiditySeconds(authentication.getOAuth2Request());
                if (validitySeconds > 0)
                {
                    token.setExpiration(new Date(System.currentTimeMillis() + (validitySeconds * 1000L)));
                }
                tokenStore.storeAccessToken(existingAccessToken, authentication);
                return existingAccessToken;
            }
        }
        return super.createAccessToken(authentication);
    }
}
