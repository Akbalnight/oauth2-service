package com.assd.oauthservice.storage;

import com.assd.oauthservice.dto.QueryData;
import com.assd.oauthservice.logger.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

import java.util.List;

/**
 * Description: Компонент для определения доступа к запросу по списку пермиссий пользователя
 * @author AsMatveev
 */
@Component
public class AccessChecker
{
    private final AntPathMatcher matcher = new AntPathMatcher();
    @Autowired
    private Logger logger;

    /**
     * Проверка доступа к запросу по списку пермиссий
     * @param permissions Список пермиссий
     * @param queryData   Данные запроса
     * @return Возвращает true если хотя бы одна из пермиссий имеет достпуп к запросу
     */
    public boolean isHaveAccess(List<Permission> permissions, QueryData queryData)
    {
        if (verifyQueryData(queryData))
        {
            return matchPermission(permissions, queryData.getMethod(), queryData.getPath());
        }
        // Если данные запроса не указаны, то не проверяем доступ
        return true;
    }

    private boolean matchPermission(List<Permission> permissions, HttpMethod method, String path)
    {
        return permissions.stream()
                          .anyMatch(permission -> permission.getMethod() == method && matcher.match(permission.getPath(), path));
    }

    private boolean verifyQueryData(QueryData queryData)
    {
        if (queryData == null || queryData.getMethod() == null || queryData.getPath() == null)
        {
            logger.debug("При проверке доступа не были указаны параметры запроса!");
            return false;
        }
        return true;
    }
}