package com.assd.oauthservice.database;

import com.assd.oauthservice.exceptions.UserNotFoundException;
import com.assd.oauthservice.logger.Logger;
import com.assd.oauthservice.resourcemanager.ResourceManager;
import com.assd.oauthservice.storage.Permission;
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
 * Date: 3 dec 2020 г.
 * Users: av.eliseev
 * Description: Реализация методов работы с БД пользователей
 */
@Component
public class UsersDaoImpl implements UsersDao
{
    private static final String SQL_SELECT_ALL_LDAP_ROLES = "" +
            "SELECT ldap_group, b.name as role_name \n" +
            "FROM ldap_roles a \n" +
            "    JOIN roles b ON b.id = a.role_id ";

    private static final String SQL_SELECT_PERMISSIONS_BY_USERNAME = "" +
            "SELECT DISTINCT a.method, a.path \n" +
            "    FROM permissions a \n" +
            "        JOIN role_permissions b ON b.permission_id = a.id \n" +
            "        JOIN user_roles c on c.role_id = b.role_id \n" +
            "        join users d on d.id = c.user_id \n" +
            "WHERE d.username = :username";

    private static final String SQL_SELECT_USER_ID_BY_USERNAME = "SELECT id FROM users WHERE username = :username";

    private static final String SQL_SELECT_ROLE_ID_BY_NAME = "SELECT id FROM roles WHERE name = :name";


    private static final String SQL_SELECT_ROLES_BY_USERNAME = "" +
            "SELECT c.name \n" +
            "    from users a \n" +
            "        join user_roles b on b.user_id = a.id \n" +
            "        join roles c on c.id = b.role_id \n" +
            "WHERE a.username = :username";

    private static final String SQL_GET_PERMISSIONS_FROM_ROLES = "" +
            "SELECT DISTINCT a.id, a.method, a.path \n" +
            "    FROM permissions a \n" +
            "        JOIN role_permissions b ON b.permission_id = a.id \n" +
            "        join roles c on c.id = b.role_id \n" +
            "WHERE c.name IN (:roles)";

    private static final String SQL_SELECT_NOT_LDAP_USER_ROLES = "" +
            "SELECT c.name \n" + // as role_name, c.id as role_id
            "    from users a \n" +
            "    JOIN user_roles b on b.user_id = a.id \n" +
            "        JOIN roles c ON c.id = b.role_id \n" +
            "WHERE a.username = :username";

    private static final String SQL_DELETE_USER_ROLES = "DELETE FROM user_roles WHERE user_id = :userId";

    private static final String SQL_INSERT_USER_ROLES = "INSERT INTO user_roles (role_id, user_id) VALUES (:roleId, :userId)";

    private static final String SQL_INSERT_LDAP_USER = "" +
            "INSERT INTO users (username, password, enabled, ldap, email, json_data) \n" +
            "    VALUES ( :username, :password, :enabled, :ldap, :email, cast(:jsonData AS JSON)) returning id;";



    @Autowired
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    private Logger logger;

    @Autowired
    private ResourceManager resources;

    /**
     * Возвращает сопоставления LDAP групп c ролями пользователей из БД
     */
    @Override
    public HashMap<String, String> getLdapAuthoritiesMap()
    {
        return jdbcTemplate.query(SQL_SELECT_ALL_LDAP_ROLES, rs ->

        {
            HashMap<String, String> mapRet = new HashMap<>();
            while (rs.next())
            {
                mapRet.put(rs.getString("ldap_group"), rs.getString("role_name"));
            }
            return mapRet;
        });
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
            logger.error("Ошибка при получении прав доступа пользователя " + login, e);
            return Collections.emptyList();
        }
    }

    @Override
    public UUID getUserId(String login)
    {
        try
        {
            return jdbcTemplate.queryForObject(SQL_SELECT_USER_ID_BY_USERNAME, new MapSqlParameterSource("username", login),
                    UUID.class);
        }
        catch (EmptyResultDataAccessException e)
        {
            throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, login));
        }
    }

    private UUID getRoleId(String name)
    {
        try
        {
            return jdbcTemplate.queryForObject(SQL_SELECT_ROLE_ID_BY_NAME, new MapSqlParameterSource("name", name),
                    UUID.class);
        }
        catch (EmptyResultDataAccessException e)
        {
            throw new UserNotFoundException(resources.getResource(ResourceManager.USER_NOT_FOUND, name));
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
            return jdbcTemplate.query(SQL_GET_PERMISSIONS_FROM_ROLES, new MapSqlParameterSource("roles", roles), (ResultSet rs) ->
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
        UUID userId = getUserId(username);
        clearUserRoles(userId);
        addRolesToUser(userId, roles);
    }

    @Override
    public List<String> getNotLdapUserRoles(String username)
    {
        return jdbcTemplate.queryForList(SQL_SELECT_NOT_LDAP_USER_ROLES, new MapSqlParameterSource("username", username),
                String.class);

//        return jdbcTemplate.query(SQL_SELECT_NOT_LDAP_USER_ROLES, rs ->
//
//        {
//            HashMap<String, UUID> mapRet = new HashMap<>();
//            while (rs.next())
//            {
//                mapRet.put(rs.getString("role_name"), UUID.fromString(rs.getString("role_id")));
//            }
//            return mapRet;
//        });
    }

    /**
     * Добавляет роли пользователю
     * @param userId Логин пользователя
     * @param roles Список ролей
     */
    private void addRolesToUser(UUID userId, Set<String> roles)
    {
        List<Map<String, Object>> batchValues = new ArrayList<>(roles.size());
        for (String role : roles)
        {
            batchValues.add(new MapSqlParameterSource().addValue("roleId", getRoleId(role))
                                                       .addValue("userId", userId)
                                                       .getValues());
        }
        jdbcTemplate.batchUpdate(SQL_INSERT_USER_ROLES, batchValues.toArray(new Map[roles.size()]));
    }

    /**
     * Очищает все роли пользователя
     * @param userId Логин пользователя
     */
    private void clearUserRoles(UUID userId)
    {
        jdbcTemplate.update(SQL_DELETE_USER_ROLES, new MapSqlParameterSource("userId", userId));
    }

    @Override
    public UUID addUserFromLdap(String username, Map<String, String> userInfo)
    {
        String email = userInfo.get(MAIL);
        if (email != null)
        {
            userInfo.remove(MAIL);
        }

        String json = null;
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("username", username);
        params.addValue("password", new BCryptPasswordEncoder().encode(""));
        params.addValue("enabled", true);
        params.addValue("ldap", true);
        params.addValue("email", email);
        params.addValue("jsonData", json);
        UUID id = jdbcTemplate.queryForObject(SQL_INSERT_LDAP_USER, params, UUID.class);
        logger.info(String.format("----->>>>> addUserFromLdap [%s]: [%s]", username, id));
        return id;
    }
}
