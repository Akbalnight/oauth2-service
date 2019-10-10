package com.assd.oauthservice.database;

import com.assd.oauthservice.datasource.DataSourceManager;
import com.assd.oauthservice.exceptions.UserNotFoundException;
import com.assd.oauthservice.logger.Logger;
import com.assd.oauthservice.resourcemanager.ResourceManager;
import com.assd.oauthservice.storage.Permission;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.ResultSet;
import java.util.*;

import static com.assd.oauthservice.ldap.LdapAttributesConst.MAIL;

/**
 * UsersDaoImpl.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Реализация методов работы с БД пользователей
 */
@Component
public class UsersDaoImpl implements UsersDao
{
    public static final String DB_NAME = "users";

    private static final String SQL_SELECT_USER_ID_BY_USERNAME = "SELECT user_id FROM users WHERE username = :username";

    final String SQL_SELECT_ROLES_BY_USERNAME = "SELECT role FROM user_roles WHERE username = :username";

    private static final String SQL_INSERT_USER_ROLES = "INSERT INTO user_roles (username, role) VALUES (:username, :role)";
    private static final String SQL_DELETE_USER_ROLES = "DELETE FROM user_roles WHERE username=:username";
    private static final String SQL_SELECT_ALL_LDAP_ROLES = "SELECT ldap_group, role FROM ldap_roles";

    private static final String SQL_SELECT_PERMISSIONS_BY_USERNAME =
            "SELECT DISTINCT p.method, p.path FROM permissions AS p JOIN role_permissions AS rp ON " +
                    "p.id=rp.id_permission JOIN user_roles AS ur on rp.role=ur.role WHERE ur.username = :username";

    private static final String SQL_INSERT_LDAP_USER = "INSERT INTO users(username,password,enabled,email,json_data,ldap) VALUES " +
            "(:username,:password,:enabled,:email,cast(:jsonData AS JSON),:ldap)";
    private static final String SQL_SELECT_NOT_LDAP_USER_ROLES = "SELECT role FROM user_roles LEFT JOIN roles ON user_roles.role = roles.name " +
            "WHERE user_roles.username = :username AND (roles.json_data->>'ldap')::boolean = false";

    private final NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private Logger logger;
    @Autowired
    private ResourceManager resources;
    @Autowired
    private ObjectMapper jsonMapper;

    @Autowired
    public UsersDaoImpl(DataSourceManager dataSourceManager)
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSourceManager.getDataSource(DB_NAME));
    }

    @Override
    public List<Permission> getUserPermissions(String login)
    {
        try
        {
            return jdbcTemplate.query(SQL_SELECT_PERMISSIONS_BY_USERNAME, new MapSqlParameterSource("username", login),
                    (ResultSet rs) ->
            {
                List<Permission> permissions = new ArrayList<>();
                while (rs.next())
                {
                    Permission permission = new Permission();
                    permission.setPath(rs.getString("path"));
                    permission.setMethod(HttpMethod.resolve(rs.getString("method")));
                    permissions.add(permission);
                }
                return permissions;
            });
        }
        catch (Exception e)
        {
            logger.error("Ошибка при получении ролей пользователя " + login, e);
            return Collections.emptyList();
        }
    }

    @Override
    public Integer getUserId(String login)
    {
        try
        {
            return jdbcTemplate.queryForObject(SQL_SELECT_USER_ID_BY_USERNAME, new MapSqlParameterSource("username", login),
                    Integer.class);
        }
        catch (EmptyResultDataAccessException e)
        {
            throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, login));
        }
    }

    @Override
    public List<String> getUserRoles(String login)
    {
        return jdbcTemplate.queryForList(SQL_SELECT_ROLES_BY_USERNAME, new MapSqlParameterSource("username", login),
                String.class);
    }

    @Override
    public List<Permission> getPermissionsFromRoles(List<String> roles)
    {
        if (roles.isEmpty())
        {
            return Collections.emptyList();
        }
        try
        {
            StringJoiner joiner = new StringJoiner(",");
            roles.forEach(role -> joiner.add("'" + role + "'"));

            final String SQL_GET_PERMISSIONS_FROM_ROLES =
                    "SELECT DISTINCT p.id, p.method, p.path FROM permissions AS p JOIN role_permissions AS rp ON" +
                            " p.id = rp.id_permission WHERE rp.role IN (" + joiner.toString() + ")";
            return jdbcTemplate.query(SQL_GET_PERMISSIONS_FROM_ROLES, (ResultSet rs) ->
            {
                List<Permission> permissions = new ArrayList<>();
                while (rs.next())
                {
                    Permission permission = new Permission();
                    permission.setPath(rs.getString("path"));
                    permission.setMethod(HttpMethod.resolve(rs.getString("method")));
                    permissions.add(permission);
                }
                return permissions;
            });
        }
        catch (Exception e)
        {
            logger.error("Ошибка при получении пермиссий по списку ролей.", e);
            return Collections.emptyList();
        }
    }

    @Transactional
    @Override
    public void updateUserRoles(String username, Set<String> roles)
    {
        clearUserRoles(username);
        addRolesToUser(username, roles);
    }

    @Override
    public List<String> getNotLdapUserRoles(String username)
    {
        return jdbcTemplate.queryForList(SQL_SELECT_NOT_LDAP_USER_ROLES, new MapSqlParameterSource("username", username),
                String.class);
    }

    /**
     * Добавляет роли пользователю
     * @param username Логин пользователя
     * @param roles Список ролей
     */
    private void addRolesToUser(String username, Set<String> roles)
    {
        List<Map<String, Object>> batchValues = new ArrayList<>(roles.size());
        for (String role : roles)
        {
            batchValues.add(new MapSqlParameterSource().addValue("role", role)
                                                       .addValue("username", username)
                                                       .getValues());
        }
        jdbcTemplate.batchUpdate(SQL_INSERT_USER_ROLES, batchValues.toArray(new Map[roles.size()]));
    }

    /**
     * Очищает все роли пользователя
     * @param username Логин пользователя
     */
    private void clearUserRoles(String username)
    {
        jdbcTemplate.update(SQL_DELETE_USER_ROLES, new MapSqlParameterSource("username", username));
    }

    @Override
    public Integer addUserFromLdap(String username, Map<String, String> userInfo)
    {
        String email = userInfo.get(MAIL);
        if (email != null)
        {
            userInfo.remove(MAIL);
        }

        String json = null;
        try
        {
            json = jsonMapper.writeValueAsString(userInfo);
        }
        catch (JsonProcessingException e)
        {
            logger.error(e);
        }
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", username);
        params.addValue("password", new BCryptPasswordEncoder().encode(""));
        params.addValue("email", email);
        params.addValue("enabled", true);
        params.addValue("ldap", true);
        params.addValue("jsonData", json);
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(SQL_INSERT_LDAP_USER, params, keyHolder, new String[]{"user_id"});
        return keyHolder.getKey().intValue();
    }

    @Override
    public HashMap<String, String> getLdapAuthoritiesMap()
    {
        return jdbcTemplate.query(SQL_SELECT_ALL_LDAP_ROLES, rs ->
        {
            HashMap<String, String> mapRet = new HashMap<>();
            while (rs.next())
            {
                mapRet.put(rs.getString("ldap_group"), rs.getString("role"));
            }
            return mapRet;
        });
    }
}