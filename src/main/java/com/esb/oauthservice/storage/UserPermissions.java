package com.esb.oauthservice.storage;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class UserPermissions
{
    private List<Permission> permissions;
}
