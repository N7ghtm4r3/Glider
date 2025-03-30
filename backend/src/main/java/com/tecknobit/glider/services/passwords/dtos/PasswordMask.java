package com.tecknobit.glider.services.passwords.dtos;

import com.tecknobit.equinoxcore.annotations.DTO;
import com.tecknobit.glider.services.passwords.entities.Password;

/**
 * The {@code PasswordMask} class is used to represent a shared password with the specific details to edit that password
 * such tail, scopes and, if the password is a {@link com.tecknobit.glidercore.enums.PasswordType#INSERTED} one, the
 * current value of the password
 *
 * @author N7ghtm4r3 - Tecknobit
 */
@DTO
public class PasswordMask {

    /**
     * {@code id} the identifier of the password
     */
    private final String id;

    /**
     * {@code tail} the tail of the password
     */
    private final String tail;

    /**
     * {@code scopes} the scopes of the password
     */
    private final String scopes;

    /**
     * {@code password} the password value
     */
    private final String password;

    /**
     * Constructor to init the {@link PasswordMask} class
     *
     * @param password The password entity from retrieve the information
     */
    public PasswordMask(Password password) {
        id = password.getId();
        tail = password.getTail();
        scopes = password.getScopes();
        this.password = password.getPassword();
    }

    /**
     * Method used to get the {@link #id} instance
     *
     * @return the {@link #id} instance as {@link String}
     */
    public String getId() {
        return id;
    }

    /**
     * Method used to get the {@link #tail} instance
     *
     * @return the {@link #tail} instance as {@link String}
     */
    public String getTail() {
        return tail;
    }

    /**
     * Method used to get the {@link #scopes} instance
     *
     * @return the {@link #scopes} instance as {@link String}
     */
    public String getScopes() {
        return scopes;
    }

    /**
     * Method used to get the {@link #password} instance
     *
     * @return the {@link #password} instance as {@link String}
     */
    public String getPassword() {
        return password;
    }

}
