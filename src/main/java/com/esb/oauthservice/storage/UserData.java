package com.esb.oauthservice.storage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserData
{
    private List<Permission> permissions;
    private UserResponseObject userResponseObject;
}
