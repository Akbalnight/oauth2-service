package com.esb.oauthservice.datasource;

/**
 * DatabaseSettings.java
 * Date: 10 сент. 2018 г.
 * Users: amatveev
 * Description: Описание парамметров подключения к базе данных
 */
public class DatabaseSettings
{
    private static int DEFAULT_MAX_POOL_SIZE = 5;
    private String driver;
    private String url;
    private String username;
    private String password;
    private int poolSize = DEFAULT_MAX_POOL_SIZE;

    /**
     * Возвращает драйвер БД
     *
     * @return возвращает драйвер БД
     */
    public String getDriver()
    {
        return driver;
    }

    /**
     * Устанавливает драйвер БД
     *
     * @param driver драйвер БД
     */
    public void setDriver(String driver)
    {
        this.driver = driver;
    }

    /**
     * Возвращает путь к БД
     *
     * @return возвращает путь к БД
     */
    public String getUrl()
    {
        return url;
    }

    /**
     * Устанавливает путь к БД
     *
     * @param url путь к БД
     */
    public void setUrl(String url)
    {
        this.url = url;
    }

    /**
     * Возвращает имя пользователя
     *
     * @return возвращает имя пользователя
     */
    public String getUsername()
    {
        return username;
    }

    /**
     * Устанавливает имя пользователя
     *
     * @param username имя пользователя
     */
    public void setUsername(String username)
    {
        this.username = username;
    }

    /**
     * Возвращает пароль пользователя
     *
     * @return возвращает пароль пользователя
     */
    public String getPassword()
    {
        return password;
    }

    /**
     * Устанавливает пароль пользователя
     *
     * @param password пароль пользователя
     */
    public void setPassword(String password)
    {
        this.password = password;
    }

    /**
     * Возвращает размер пула соединений к БД
     *
     * @return возвращает размер пула соединений к БД
     */
    public int getPoolSize()
    {
        return poolSize;
    }

    /**
     * Устанавливает размер пула соединений к БД
     *
     * @param poolSize размер пула соединений к БД
     */
    public void setPoolSize(int poolSize)
    {
        this.poolSize = poolSize;
    }
}
