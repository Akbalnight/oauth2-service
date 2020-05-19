package com.assd.oauthservice.service;

import com.assd.oauthservice.dto.QueryData;
import com.assd.oauthservice.dto.UserResponseObject;
//import com.assd.oauthservice.exceptions.BadRequestException;
import com.assd.oauthservice.exceptions.BadRequestException;
import com.assd.oauthservice.exceptions.ForbiddenQueryException;
import com.assd.oauthservice.resourcemanager.ResourceManager;
import com.assd.oauthservice.storage.AccessChecker;
import com.assd.oauthservice.dto.UserDTO;
import com.assd.oauthservice.exceptions.ServiceException;
import com.assd.oauthservice.exceptions.UserNotFoundException;
import com.assd.oauthservice.userdetails.AssdUserDetails;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidClientException;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;
import org.springframework.security.oauth2.provider.error.DefaultWebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.error.WebResponseExceptionTranslator;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Date;
import java.util.List;

/**
 * Date: 19 may 2020 г.
 * Users: av.eliseev
 * Description: Сервис для работы с пользователями
 */
@Log4j2
@Service
public class AuthService
{

    /**
     * Продолжительность жизни токена в секундах
     */
    @Value("${auth.access_token.expired.seconds}")
    private int accessTokenExpiredSeconds;

    @Autowired
    private AccessChecker accessChecker;

    @Autowired
    private TokenStore tokenStore;

    @Autowired
    private DefaultTokenServices tokenServices;

    @Autowired
    private ResourceManager resources;

    /**
     * Проверка токена и уникальности клиента
     * @param value          Данные аутентификации пользователя
     * @param code_challenge Данные уникальности клиента
     * @return Возвращает 200 или исключение 400 / 401
     */
    public ResponseEntity<?> checkToken(String value, String code_challenge){
        checkToken(tokenServices.readAccessToken(value), code_challenge);
        return ResponseEntity.ok().build();
    }

    /**
     * Проверка токена и уникальности клиента
     * @param OAuth2Token    Токен пользователя
     * @param code_challenge Данные уникальности клиента
     */
    private void checkToken(OAuth2AccessToken OAuth2Token, String code_challenge)
    {
        if (OAuth2Token == null) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "invalid_token", "Token was not recognised");
        }
        if (OAuth2Token.isExpired()) {
            throw new ServiceException(HttpStatus.BAD_REQUEST, "invalid_token", "Token has expired");
        }

        checkCodeChallenge(OAuth2Token, code_challenge);
    }

    private void checkCodeChallenge(OAuth2AccessToken OAuth2Token, String code_challenge)
    {
        String token_code_challenge = OAuth2Token.getAdditionalInformation().get("code_challenge").toString();

        if(!token_code_challenge.equals(code_challenge)){
            throw new ServiceException(HttpStatus.BAD_REQUEST, "invalid_client", "Code challenge does not match with token");
        }
    }

    /**
     * Проверка доступа пользователя к запросу
     * @param value     Token пользователя в заголовке запроса
     * @param queryData Данные запроса
     * @return Возвращает информацию {@code UserResponseObject} о пользователе если у него есть доступ к запросу
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public ResponseEntity<?> checkAccess(String value, String code_challenge, QueryData queryData) throws ServiceException
    {
        OAuth2AccessToken OAuth2Token = tokenServices.readAccessToken(value);

        checkToken(OAuth2Token, code_challenge);

        OAuth2Authentication authentication = tokenServices.loadAuthentication(OAuth2Token.getValue());

        AssdUserDetails userData = findUserData(authentication);

        if (accessChecker.isHaveAccess(userData.getPermissions(), queryData))
        {
            DefaultOAuth2AccessToken token = (DefaultOAuth2AccessToken) OAuth2Token;
            int validitySeconds =  accessTokenExpiredSeconds;//OAuth2Token.getExpiresIn();
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
                tokenStore.storeAccessToken(OAuth2Token, authentication);
            }

//            log.info("validitySeconds           => [{}]", validitySeconds);
//            log.info("OAuth2Token.getExpiration => [{}]", OAuth2Token.getExpiration());
//            log.info("token.getExpiration       => [{}]", token.getExpiration());

            return new ResponseEntity<>(UserResponseObject.builder()
                    .username(userData.getName())
                    .id(userData.getUserId())
                    .roles(userData.getRoles())
                    .code_challenge(OAuth2Token.getAdditionalInformation().get("code_challenge").toString())
                    .build(), HttpStatus.OK);
        }
        throw new ForbiddenQueryException(resources.getResource(ResourceManager.FORBIDDEN_QUERY, authentication.getName(),
                queryData.getMethod(), queryData.getPath()));
    }

    /**
     * Удаляет токены текущего пользователя
     * @param value
     * @param code_challenge
     */
    public ResponseEntity<?> logout(String value, String code_challenge)
    {
        OAuth2AccessToken userToken = tokenServices.readAccessToken(value);
//        checkToken(userToken, code_challenge);
        checkCodeChallenge(userToken, code_challenge);
        if(tokenServices.revokeToken(userToken.getValue()))
            return ResponseEntity.ok().build();
        else
            return ResponseEntity.badRequest().build();
    }

    /**
     * Возвращает всех пользователей с активными токенами доступа в рамках сервиса клиента
     * @param authentication Данные клиента
     * @return Возвращает список активных пользователей
     * @throws ServiceException Исключение если у текущего пользователя нет доступа к методу
     */
    public List<UserDTO> getActiveUsers(Authentication authentication)
            throws ServiceException
    {
        return null;
//        return tokenStore.getActiveUsers(authentication.getOAuth2Request()
//                                                       .getClientId());
    }

    /**
     * Удаляет accessToken и refreshToken указанного пользователя
     * @param clientId Id клиента текущего пользователя
     * @param username Логин пользователя для удаления токенов
     * @param userId   Id пользователя для удаления токенов
     * @throws ServiceException Исключение если пользователь не найден
     */
    public void revokeTokenForUser(String clientId, String username, Integer userId)
            throws ServiceException
    {
        if (username == null)
        {
            throw new BadRequestException(resources.getResource(ResourceManager.USER_NAME_NOT_SPECIFIED));
        }

        OAuth2AccessToken token = null; //tokenStore.getTokenForUser(clientId, username, userId);
        if (token == null)
        {
            throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, username));
        }
        tokenServices.revokeToken(token.getValue());
    }

    /**
     * Проверяет доступ пользователя к запросу
     * @param authentication Данные аутентификации пользователя
     * @param query          Данные запроса
     * @throws ServiceException Исключение если у пользователя нет доступа к запросу
     */
    public void verifyAccess(Authentication authentication, QueryData query)
            throws ServiceException
    {
        AssdUserDetails currentUser = findUserData(authentication);
        if (!accessChecker.isHaveAccess(currentUser.getPermissions(), query))
        {
            throw new ForbiddenQueryException(resources.getResource(ResourceManager.FORBIDDEN_QUERY, currentUser.getName(),
                    query.getMethod(), query.getPath()));
        }
    }

    /**
     * Возвращет объект {@link AssdUserDetails} текущего пользователя
     * @param authentication Данные аутентификации пользователя
     * @return Возвращает данные пользователя
     * @throws UserNotFoundException Исключение при ошибке получения данных аутентификации пользователя
     */
    private AssdUserDetails findUserData(Authentication authentication)
            throws UserNotFoundException
    {
        if (authentication.getPrincipal() instanceof AssdUserDetails)
        {
            return (AssdUserDetails) authentication.getPrincipal();
        }
        throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, authentication.getName()));
    }
}
