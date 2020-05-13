package com.assd.oauthservice.controllers;

import com.assd.oauthservice.dto.QueryData;
import com.assd.oauthservice.dto.UserDTO;
import com.assd.oauthservice.exceptions.ForbiddenQueryException;
import com.assd.oauthservice.exceptions.ServiceException;
import com.assd.oauthservice.resourcemanager.ResourceManager;
import com.assd.oauthservice.service.AuthService;
import com.assd.oauthservice.userdetails.AssdUserDetails;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;
import java.util.List;

/**
 * AuthController.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Основной контроллер сервиса
 */
@Controller
class AuthController
{
    @Autowired
    private AuthService service;

    @GetMapping("/login")
    public String login() {
        return "/login";
    }

    /**
     * Проверка доступа пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param queryData Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    @PostMapping(value = "/oauth/checkAccess")
    public ResponseEntity<?> checkAccess(
            OAuth2Authentication authentication,
//            Principal principal,
//            HttpSession session,
//            @AuthenticationPrincipal AssdUserDetails userDetails,
            @RequestHeader (name="Authorization") String authorization,
            @RequestBody QueryData queryData)
            throws ServiceException
    {
        if(authorization != null){
            String[] token = authorization.split(" ");
            return service.checkAccess(token[1], queryData);
//            return service.checkAccess(authentication, queryData);
        } else {
            return ResponseEntity.status(401).build();
        }
//        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
//        OAuth2Authentication oAuth2Authentication = (OAuth2Authentication) SecurityContextHolder.getContext() .getAuthentication();
//        Authentication userAuthentication = oAuth2Authentication.getUserAuthentication();
//        return service.checkAccess(authentication, queryData);

//        SecurityContext context = (SecurityContext)session.getAttribute("SPRING_SECURITY_CONTEXT");
//        String  login = ((org.springframework.security.core.userdetails.User) context.getAuthentication().getPrincipal()).getUsername(); // = "user"
//        return ResponseEntity.ok(context.getAuthentication().getPrincipal());
    }

//    @PostMapping(value = "/checkAccess")
//    public String checkAccess(HttpServletRequest request, @RequestBody QueryData queryData)
//            throws ServiceException
//    {
//        Principal principal = request.getUserPrincipal();
//        return principal.getName();
//    }

    /**
     * Удаляет токены текущего пользователя
     * @param authentication Данные аутентификации пользователя
     */
    @GetMapping("/oauth/revokeToken")
    @ResponseStatus(code = HttpStatus.OK)
    public void logout(OAuth2Authentication authentication)
    {
        service.logout(authentication);
    }

    /**
     * Удаляет accessToken и refreshToken указанного пользователя
     * @param authentication Данные аутентификации текущего пользователя
     * @param userData Логин и Id пользователя. Для LDAP пользователей Id = null
     */
    @PostMapping("/oauth/revokeTokenForUser")
    public void revokeTokenByUsername(OAuth2Authentication authentication, @RequestBody UserDTO userData)
    {
        service.verifyAccess(authentication,  QueryData.builder()
                                               .method(HttpMethod.POST)
                                               .path("/oauth/revokeTokenForUser")
                                               .build());
        service.revokeTokenForUser(authentication.getOAuth2Request().getClientId(), userData.getUsername(), userData.getId());
    }

    /**
     * Возвращает всех пользователей с активными токенами доступа в рамках сервиса клиента
     * @param authentication Данные клиента
     * @return Возвращает список активных пользователей
     * @throws ServiceException Исключение елси у текущего пользователя нет доступа к методу
     */
    @GetMapping(value = "/oauth/activeUsers")
    public List<UserDTO> getActiveUsers(Authentication authentication)
            throws ServiceException
    {
        service.verifyAccess(authentication, QueryData.builder()
                                            .method(HttpMethod.GET)
                                            .path("/oauth/activeUsers")
                                            .build());
        return service.getActiveUsers(authentication);
    }
}
