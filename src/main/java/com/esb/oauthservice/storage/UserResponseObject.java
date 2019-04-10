package com.esb.oauthservice.storage;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class UserResponseObject
{
    private List<String> roles;
    private int id;
}
