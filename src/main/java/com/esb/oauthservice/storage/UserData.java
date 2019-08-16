package com.esb.oauthservice.storage;

import com.esb.oauthservice.dto.UserResponseObject;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * UserData.java
 * Date: 10 апр. 2019 г.
 * Users: amatveev
 * Description: Данные пользователя
 */
@Getter
@Setter
@Builder
public class UserData
{
    /**
     * Список пермиссий пользователя
     */
    private List<Permission> permissions;
    /**
     * Возвращаемые клиенту данные пользователя
     */
    private UserResponseObject userResponseObject;
}
