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
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.List;

/**
 * AuthController.java
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Основной контроллер сервиса
 */
@Controller
class AuthController
{
    @Autowired
    private AuthService service;

    @GetMapping("/oauth/login")
    public String login() {
        return "/login";
    }

    @GetMapping("/oauth/home")
    public String home() {
        return "/home";
    }

    /**
     * Проверка токена и уникальности клиента
     * @param token          Данные аутентификации пользователя
     * @return Возвращает 200 или исключение 400 / 401
     * @throws ServiceException Исключение если у пользователя проблемы с токеном или клиентом
     */
    @PostMapping(value = "/oauth/checkToken")
    public ResponseEntity<?> checkToken(
            HttpServletRequest httpRequest,
            @RequestParam("token") String token)
            throws ServiceException, UnsupportedEncodingException {
        String code_challenge = getCodeChallenge(httpRequest);
        if(token != null && code_challenge != null){
            String decode_code = URLDecoder.decode(code_challenge, StandardCharsets.UTF_8.toString());
            return service.checkToken(token, decode_code);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Проверка доступа пользователя к запросу
     * @param token Данные аутентификации пользователя
     * @param queryData Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    @PostMapping(value = "/oauth/checkAccess")
    public ResponseEntity<?> checkAccess(
            @RequestParam("token") String token,
            @RequestParam("code_challenge") String code_challenge,
            @RequestBody QueryData queryData)
            throws ServiceException, UnsupportedEncodingException {
        if(token != null && code_challenge != null){
            String decode_code = URLDecoder.decode(code_challenge, StandardCharsets.UTF_8.toString());
            return service.checkAccess(token, decode_code, queryData);
        } else {
            return ResponseEntity.status(401).build();
        }
    }

    /**
     * Удаляет токены текущего пользователя
     * @param token
     * @return
     * @throws UnsupportedEncodingException
     */
    @GetMapping("/oauth/revokeToken")
    @ResponseStatus(code = HttpStatus.OK)
//    public void logout(OAuth2Authentication authentication)
    public ResponseEntity<?> logout(
            HttpServletRequest httpRequest,
            @RequestParam("token") String token
    ) throws UnsupportedEncodingException
    {
        String code_challenge = getCodeChallenge(httpRequest);
        if(token != null && code_challenge != null){
            String decode_code = URLDecoder.decode(code_challenge, StandardCharsets.UTF_8.toString());
            return service.logout(token, decode_code);
        } else {
            return ResponseEntity.status(401).build();
        }
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

    /**
     * Формирует параметр дополнитльной проверки доступа
     */
    private String getCodeChallenge(HttpServletRequest httpRequest) {
        if (httpRequest != null) {
            Cookie[] cookies = httpRequest.getCookies();
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals("code_challenge")) {
                    return cookies[i].getValue();
                }
            }
        }
        return null;
    }
}
