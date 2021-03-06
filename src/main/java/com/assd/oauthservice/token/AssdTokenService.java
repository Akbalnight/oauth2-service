package com.assd.oauthservice.token;

import com.assd.oauthservice.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionFailedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * @author AsMatveev
 * Description: Переопределяет {@code DefaultTokenServices}
 */
public class AssdTokenService
        extends DefaultTokenServices
{
    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private Logger logger;
    /**
     * Обновляет время жизни accessToken при каждом запросе аутентификации
     */
    @Override
    @Transactional
    public OAuth2AccessToken createAccessToken(OAuth2Authentication authentication)
            throws AuthenticationException
    {
        try
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
                        Date date = new Date(System.currentTimeMillis() + (validitySeconds * 1000L));
                        DefaultExpiringOAuth2RefreshToken refreshToken = (DefaultExpiringOAuth2RefreshToken) token.getRefreshToken();
                        if (date.after(refreshToken.getExpiration()))
                        {
                            // Если refreshToken истекает раньше date, установим его дату
                            date = refreshToken.getExpiration();
                        }
                        token.setExpiration(date);
                        tokenStore.storeAccessToken(existingAccessToken, authentication);
                    }
                    return existingAccessToken;
                }
            }
            return super.createAccessToken(authentication);
        }
        catch (ConversionFailedException ex)
        {
            logger.error("Ошибка при создании токена", ex);
            throw ex;
        }
    }
}
