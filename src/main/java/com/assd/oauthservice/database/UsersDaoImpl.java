package com.assd.oauthservice.database;

import com.assd.oauthservice.exceptions.UserNotFoundException;
import com.assd.oauthservice.logger.Logger;
import com.assd.oauthservice.resourcemanager.ResourceManager;
import com.assd.oauthservice.datasource.DataSourceManager;
import com.assd.oauthservice.storage.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.util.*;

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
    private final NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private Logger logger;
    @Autowired
    private ResourceManager resources;

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
            final String SQL_GET_PERMISSIONS_BY_USERNAME =
                    "SELECT DISTINCT p.method, p.path FROM permissions AS p JOIN role_permissions AS rp ON " +
                            "p.id=rp.id_permission JOIN user_roles AS ur on rp.role=ur.role WHERE ur.username = :username";
            return jdbcTemplate.query(SQL_GET_PERMISSIONS_BY_USERNAME, new MapSqlParameterSource("username", login),
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
            final String SQL_GET_USER_ID = "SELECT user_id FROM users WHERE username = :username";
            return jdbcTemplate.queryForObject(SQL_GET_USER_ID, new MapSqlParameterSource("username", login),
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
        final String SQL_GET_USER_ROLES = "SELECT role FROM user_roles WHERE username = :username";
        return jdbcTemplate.queryForList(SQL_GET_USER_ROLES, new MapSqlParameterSource("username", login),
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

    @Override
    public HashMap<String, String> getLdapAuthoritiesMap()
    {
        final String SQL_GET_LDAP_ROLES = "SELECT ldap_group, role FROM ldap_roles";
        return jdbcTemplate.query(SQL_GET_LDAP_ROLES, rs ->
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