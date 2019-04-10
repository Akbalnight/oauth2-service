package com.esb.oauthservice.database;

import com.esb.oauthservice.datasource.DataSourceManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.nio.charset.StandardCharsets;

@Component
public class TokenDaoImpl
{
    public static final String TOKEN_DB = "auth";

    private static final String SQL_VALIDATE_TOKENS = "SELECT token_id FROM oauth_access_token LIMIT 1";
    private static final String SQL_VALIDATE_REFRESH_TOKENS = "SELECT token_id FROM oauth_refresh_token LIMIT 1";
    private static final String RESOURCE_CREATE_TABLES = "/sql/createOauth2Tables.sql";


    private JdbcTemplate jdbcTemplate;

    @Autowired
    public TokenDaoImpl(DataSourceManager dataSourceManager)
    {
        jdbcTemplate = new JdbcTemplate(dataSourceManager.getDataSource(TOKEN_DB));
    }

    @PostConstruct
    private void init()
    {
        if (!validateTables())
        {
            try
            {
                createTables();
            }
            catch (IOException e)
            {
                //logger.error("Ошибка инициализации базы данных пользователей и пермиссий", e);
            }
        }
    }

    private void createTables()
            throws IOException
    {
        executeSqlFromFile(RESOURCE_CREATE_TABLES);
    }

    /**
     * Выполняет SQL скрипт из указанного файла
     * @param path
     * @throws IOException
     */
    private void executeSqlFromFile(String path)
            throws IOException
    {
        InputStreamReader streamReader = new InputStreamReader(getClass().getResourceAsStream(path),
                StandardCharsets.UTF_8);
        LineNumberReader reader = new LineNumberReader(streamReader);
        String query = ScriptUtils.readScript(reader, "--", ";");
        jdbcTemplate.execute(query);
    }

    private boolean validateTables()
    {
        try
        {
            jdbcTemplate.queryForList(SQL_VALIDATE_TOKENS, String.class);
            jdbcTemplate.queryForList(SQL_VALIDATE_REFRESH_TOKENS, String.class);
        }
        catch (DataAccessException ex)
        {
            return false;
        }
        return true;
    }
}
