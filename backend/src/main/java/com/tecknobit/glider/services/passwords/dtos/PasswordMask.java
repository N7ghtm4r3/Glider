package com.tecknobit.glider.services.passwords.dtos;

import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.glider.services.passwords.entities.Password;

@DTO
public class PasswordMask {

    private final String id;

    private final String tail;

    private final String scopes;

    private final String password;

    public PasswordMask(Password password) {
        id = password.getId();
        tail = password.getTail();
        scopes = password.getScopes();
        this.password = password.getPassword();
    }

    public String getId() {
        return id;
    }

    public String getTail() {
        return tail;
    }

    public String getScopes() {
        return scopes;
    }

    public String getPassword() {
        return password;
    }
}
