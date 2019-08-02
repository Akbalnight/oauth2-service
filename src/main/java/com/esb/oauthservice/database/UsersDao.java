package com.esb.oauthservice.database;

import com.esb.oauthservice.storage.Permission;

import java.util.List;
import java.util.Map;

public interface UsersDao
{
    Map<String, String> getLdapAuthoritiesMap();

    List<Permission> getUserPermissions(String login);

    Integer getUserId(String login);

    List<String> getUserRoles(String login);
}
