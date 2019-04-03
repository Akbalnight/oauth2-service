package com.esb.oauthservice.controllers;

import com.esb.oauthservice.config.Const;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
public class AdminController
{
    @Autowired
    private TokenStore tokenStore;

    @GetMapping("/token/list")
    @Secured({"ROLE_ADMIN"})
    public List<String> findAllTokens()
    {
        final Collection<OAuth2AccessToken> tokensByClientId = tokenStore.findTokensByClientId(Const.CLIENT_ID);

        return tokensByClientId.stream().map(token -> token.getValue()).collect(Collectors.toList());
    }
}
