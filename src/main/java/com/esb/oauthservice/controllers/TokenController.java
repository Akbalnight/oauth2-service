package com.esb.oauthservice.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TokenController
{

    @Autowired
    private DefaultTokenServices tokenServices;

    @DeleteMapping("/oauth/revoke")
    private boolean revokeToken(Authentication authentication)
    {
        final String userToken = ((OAuth2AuthenticationDetails) authentication.getDetails()).getTokenValue();
        return tokenServices.revokeToken(userToken);
    }
}
