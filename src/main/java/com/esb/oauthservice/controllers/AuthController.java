package com.esb.oauthservice.controllers;

import com.esb.oauthservice.model.QueryData;
import com.esb.oauthservice.storage.UsersStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
//@RequestMapping("/")
public class AuthController
{
    @Autowired
    private UsersStorage usersStorage;

    @GetMapping(value = "/auth")
    //@Secured({"ROLE_USER", "ROLE_ADMIN"})
    public String auth(Principal principal/*, OAuth2AuthorizedClient authorizedClient*/)
    {
        if (principal != null)
        {
            return principal.getName() + " " + principal.toString();
        }
        return "false";
    }

    @GetMapping(value = "checkAccess")
    public ResponseEntity checkAccess(Principal principal, @RequestBody QueryData queryData)
    {
        boolean result = usersStorage.checkAccess(principal.getName(), queryData.getMethod(), queryData.getPath());
        HttpStatus status = result ? HttpStatus.OK : HttpStatus.FORBIDDEN;
        return new ResponseEntity(status);
    }
}
