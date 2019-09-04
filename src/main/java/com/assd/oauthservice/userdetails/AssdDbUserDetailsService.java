package com.assd.oauthservice.userdetails;

import com.assd.oauthservice.database.UsersDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.jdbc.JdbcDaoImpl;

/**
 * Description: Обертка {@link UserDetailsService} для приведения пользователей из БД к виду {@link AssdUser}
 * @author AsMatveev
 */
public class AssdDbUserDetailsService
        extends JdbcDaoImpl
{
    @Autowired
    private UsersDao usersDao;

    /**
     * Формирует объект {@code AssdUser} для аутентификации пользователей из БД
     * @param username Логин пользователя
     * @return Возвращает заполненную запись пользователя {@code AssdUser}
     * @throws UsernameNotFoundException Исключение если пользователь не найден в БД
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException
    {
        UserDetails user = super.loadUserByUsername(username);
        username = user.getUsername(); // Используем логин из БД для поддережки регистра
        return new AssdUser(username, user.getPassword(), user.isEnabled(),
                user.isAccountNonExpired(), user.isCredentialsNonExpired(), user.isAccountNonLocked(),
                user.getAuthorities(), usersDao.getUserId(username), usersDao.getUserPermissions(username));
    }
}
