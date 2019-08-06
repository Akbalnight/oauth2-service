package com.esb.oauthservice.database;

import com.esb.oauthservice.datasource.DataSourceManager;
import com.esb.oauthservice.logger.Logger;
import com.esb.oauthservice.storage.Permission;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
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
    private final NamedParameterJdbcTemplate jdbcTemplate;
    @Autowired
    private Logger logger;

    @Autowired
    public UsersDaoImpl(DataSourceManager dataSourceManager)
    {
        jdbcTemplate = new NamedParameterJdbcTemplate(dataSourceManager.getDataSource("users"));
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
    {//TODO добавить приведение логина в нижн регистр?
        try
        {
            final String SQL_GET_USER_ID = "SELECT user_id FROM users WHERE username = :username";
            return jdbcTemplate.queryForObject(SQL_GET_USER_ID, new MapSqlParameterSource("username", login),
                    Integer.class);
        }
        catch (EmptyResultDataAccessException e)
        {
            return 0;
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
            roles.forEach(role ->
            {
                joiner.add("'" + role + "'");
            });

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
        return jdbcTemplate.query(SQL_GET_LDAP_ROLES, new ResultSetExtractor<HashMap<String, String>>()
        {
            @Override
            public HashMap<String, String> extractData(ResultSet rs)
                    throws SQLException, DataAccessException
            {
                HashMap<String, String> mapRet = new HashMap<String, String>();
                while (rs.next())
                {
                    mapRet.put(rs.getString("ldap_group"), rs.getString("role"));
                }
                return mapRet;
            }
        });
    }
}