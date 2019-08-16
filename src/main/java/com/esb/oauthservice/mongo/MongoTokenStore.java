package com.esb.oauthservice.mongo;

import com.esb.oauthservice.dto.ActiveUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.oauth2.common.DefaultExpiringOAuth2RefreshToken;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.DefaultAuthenticationKeyGenerator;
import org.springframework.security.oauth2.provider.token.TokenStore;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static com.esb.oauthservice.storage.AdditionalInformationConst.USER_ID;

public class MongoTokenStore
        implements TokenStore
{

    private final AuthenticationKeyGenerator authenticationKeyGenerator = new DefaultAuthenticationKeyGenerator();

    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public OAuth2Authentication readAuthentication(OAuth2AccessToken accessToken)
    {
        return readAuthentication(accessToken.getValue());
    }

    @Override
    public OAuth2Authentication readAuthentication(String token)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.TOKEN_ID)
                .is(extractTokenKey(token)));

        MongoAccessToken mongoAccessToken = mongoTemplate.findOne(query, MongoAccessToken.class);
        return mongoAccessToken != null ? mongoAccessToken.getAuthentication() : null;
    }

    @Override
    public void storeAccessToken(OAuth2AccessToken accessToken, OAuth2Authentication authentication)
    {
        String refreshToken = null;
        if (accessToken.getRefreshToken() != null)
        {
            refreshToken = accessToken
                    .getRefreshToken()
                    .getValue();
        }

        if (readAccessToken(accessToken.getValue()) != null)
        {
            this.removeAccessToken(accessToken);
        }

        MongoAccessToken mongoAccessToken = new MongoAccessToken();
        mongoAccessToken.setTokenId(extractTokenKey(accessToken.getValue()));
        mongoAccessToken.setToken(accessToken);
        mongoAccessToken.setAuthenticationId(authenticationKeyGenerator.extractKey(authentication));
        mongoAccessToken.setUsername(authentication.isClientOnly() ? null : authentication.getName());
        mongoAccessToken.setClientId(authentication
                .getOAuth2Request()
                .getClientId());
        mongoAccessToken.setAuthentication(authentication);
        mongoAccessToken.setRefreshToken(extractTokenKey(refreshToken));

        mongoTemplate.save(mongoAccessToken);
    }

    @Override
    public OAuth2AccessToken readAccessToken(String tokenValue)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.TOKEN_ID)
                .is(extractTokenKey(tokenValue)));

        MongoAccessToken mongoAccessToken = mongoTemplate.findOne(query, MongoAccessToken.class);
        return mongoAccessToken != null ? mongoAccessToken.getToken() : null;
    }

    @Override
    public void removeAccessToken(OAuth2AccessToken oAuth2AccessToken)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.TOKEN_ID)
                .is(extractTokenKey(oAuth2AccessToken.getValue())));
        mongoTemplate.remove(query, MongoAccessToken.class);
    }

    @Override
    public void storeRefreshToken(OAuth2RefreshToken refreshToken, OAuth2Authentication authentication)
    {
        MongoRefreshToken token = new MongoRefreshToken();
        token.setTokenId(extractTokenKey(refreshToken.getValue()));
        token.setToken(refreshToken);
        token.setAuthentication(authentication);
        mongoTemplate.save(token);
    }

    @Override
    public OAuth2RefreshToken readRefreshToken(String tokenValue)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoRefreshToken.TOKEN_ID)
                .is(extractTokenKey(tokenValue)));

        MongoRefreshToken mongoRefreshToken = mongoTemplate.findOne(query, MongoRefreshToken.class);
        return mongoRefreshToken != null ? mongoRefreshToken.getToken() : null;
    }

    @Override
    public OAuth2Authentication readAuthenticationForRefreshToken(OAuth2RefreshToken refreshToken)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoRefreshToken.TOKEN_ID)
                .is(extractTokenKey(refreshToken.getValue())));

        MongoRefreshToken mongoRefreshToken = mongoTemplate.findOne(query, MongoRefreshToken.class);
        return mongoRefreshToken != null ? mongoRefreshToken.getAuthentication() : null;
    }

    @Override
    public void removeRefreshToken(OAuth2RefreshToken refreshToken)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoRefreshToken.TOKEN_ID)
                .is(extractTokenKey(refreshToken.getValue())));
        mongoTemplate.remove(query, MongoRefreshToken.class);
    }

    @Override
    public void removeAccessTokenUsingRefreshToken(OAuth2RefreshToken refreshToken)
    {
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.REFRESH_TOKEN)
                .is(extractTokenKey(refreshToken.getValue())));
        mongoTemplate.remove(query, MongoAccessToken.class);
    }

    @Override
    public OAuth2AccessToken getAccessToken(OAuth2Authentication authentication)
    {
        OAuth2AccessToken accessToken = null;
        String authenticationId = authenticationKeyGenerator.extractKey(authentication);

        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.AUTHENTICATION_ID)
                .is(authenticationId));

        MongoAccessToken mongoAccessToken = mongoTemplate.findOne(query, MongoAccessToken.class);
        if (mongoAccessToken != null)
        {
            accessToken = mongoAccessToken.getToken();
            if (accessToken != null && !authenticationId.equals(this.authenticationKeyGenerator.extractKey(this.readAuthentication(accessToken))))
            {
                this.removeAccessToken(accessToken);
                this.storeAccessToken(accessToken, authentication);
            }
        }
        return accessToken;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientIdAndUserName(String clientId, String username)
    {
        Collection<OAuth2AccessToken> tokens = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.CLIENT_ID)
                .is(clientId));
        query.addCriteria(Criteria
                .where(MongoAccessToken.USERNAME)
                .is(username));
        List<MongoAccessToken> accessTokens = mongoTemplate.find(query, MongoAccessToken.class);
        for (MongoAccessToken accessToken : accessTokens)
        {
            tokens.add(accessToken.getToken());
        }
        return tokens;
    }

    @Override
    public Collection<OAuth2AccessToken> findTokensByClientId(String clientId)
    {
        Collection<OAuth2AccessToken> tokens = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria
                .where(MongoAccessToken.CLIENT_ID)
                .is(clientId));
        List<MongoAccessToken> accessTokens = mongoTemplate.find(query, MongoAccessToken.class);
        for (MongoAccessToken accessToken : accessTokens)
        {
            tokens.add(accessToken.getToken());
        }
        return tokens;
    }

    private String extractTokenKey(String value)
    {
        if (value == null)
        {
            return null;
        }
        else
        {
            MessageDigest digest;
            try
            {
                digest = MessageDigest.getInstance("MD5");
            }
            catch (NoSuchAlgorithmException var5)
            {
                throw new IllegalStateException("MD5 algorithm not available.  Fatal (should be in the JDK).");
            }

            byte[] e = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return String.format("%032x", new Object[]{new BigInteger(1, e)});
        }
    }

    /**
     * Возвращает всех пользоватей сервиса с активными токенами доступа
     * @param clientId Идентификатор сервиса
     * @return Возвращает список активных пользователей
     */
    public List<ActiveUser> getActiveUsers(String clientId)
    {
        List<ActiveUser> activeUsers = new ArrayList<>();
        Query query = new Query();
        query.addCriteria(Criteria.where(MongoAccessToken.CLIENT_ID).is(clientId));
        List<MongoAccessToken> mongoAccessTokens = mongoTemplate.find(query, MongoAccessToken.class);

        for (MongoAccessToken token : mongoAccessTokens)
        {
            DefaultOAuth2AccessToken accessToken = (DefaultOAuth2AccessToken) token.getToken();
            if (!accessToken.isExpired())
            {
                DefaultExpiringOAuth2RefreshToken refreshToken = (DefaultExpiringOAuth2RefreshToken) accessToken.getRefreshToken();
                activeUsers.add(ActiveUser.builder()
                                          .id((Integer) accessToken.getAdditionalInformation().get(USER_ID))
                                          .username(token.getUsername())
                                          .accessTokenExpiration(accessToken.getExpiration())
                                          .refreshTokenExpiration(refreshToken.getExpiration())
                                          .build());
            }
        }
        return activeUsers;
    }
}