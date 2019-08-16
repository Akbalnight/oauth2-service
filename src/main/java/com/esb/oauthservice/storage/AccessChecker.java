package com.esb.oauthservice.storage;

import com.esb.oauthservice.exceptions.BadRequestException;
import com.esb.oauthservice.exceptions.ServiceException;
import com.esb.oauthservice.dto.QueryData;
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

    /**
     * Проверка доступа к запросу по списку пермиссий
     * @param permissions Список пермиссий
     * @param queryData   Данные запроса
     * @return Возвращает true если хотя бы одна из пермиссий имеет достпуп к запросу
     */
    public boolean isHaveAccess(List<Permission> permissions, QueryData queryData)
    {
        verifyQueryData(queryData);
        return matchPermission(permissions, queryData.getMethod(), queryData.getPath());
    }

    private boolean matchPermission(List<Permission> permissions, HttpMethod method, String path)
    {
        return permissions.stream()
                          .anyMatch(permission -> permission.getMethod() == method && matcher.match(permission.getPath(), path));
    }

    private void verifyQueryData(QueryData queryData)
            throws ServiceException
    {
        if (queryData == null || queryData.getMethod() == null || queryData.getPath() == null)
        {
            throw new BadRequestException("Не указаны параметры запроса!");
        }
    }
}
