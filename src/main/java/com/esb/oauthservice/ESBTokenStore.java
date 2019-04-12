package com.esb.oauthservice;

import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JdbcTokenStore;

import javax.sql.DataSource;


//@Component
public class ESBTokenStore extends JdbcTokenStore implements TokenStore
{
    public ESBTokenStore(DataSource dataSource)
    {
        super(dataSource);
    }


    @Override
    public void storeAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication)
    {
        super.storeAccessToken(token, authentication);
    }
}
