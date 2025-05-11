package com.tecknobit.glider.services.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.glider.services.passwords.entities.Password;
import jakarta.persistence.*;

import java.util.List;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.*;
import static com.tecknobit.glidercore.ConstantsKt.LAST_LOGIN_KEY;

/**
 * The {@code GliderUser} class is useful to represent a base Glider's system user
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 * @see EquinoxUser
 */
@Entity
@Table(name = USERS_KEY)
public class GliderUser extends EquinoxUser {

    /**
     * {@code devices} the devices connected to the current session of the user
     */
    @OneToMany(
            mappedBy = USER_KEY,
            cascade = CascadeType.ALL
    )
    @OrderBy(LAST_LOGIN_KEY + " DESC")
    private final List<DeviceUserSession> devices;

    /**
     * {@code passwords} the passwords owned by the user
     */
    @OneToMany(
            mappedBy = USER_KEY,
            cascade = CascadeType.ALL
    )
    @OrderBy(CREATION_DATE_KEY + " DESC")
    private final List<Password> passwords;

    /**
     * Constructor to init the {@link GliderUser} class
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public GliderUser() {
        this(null, null, null, null, null, null, null, List.of(),
                List.of());
    }

    /**
     * Constructor to init the {@link GliderUser} class
     *
     * @param id        Identifier of the user
     * @param token     The token which the user is allowed to operate on server
     * @param name      The password of the user
     * @param surname   The surname of the user
     * @param email     The password of the user
     * @param password  The password of the user
     * @param language  The password of the user
     * @param devices   The devices connected to the current session of the user
     * @param passwords The passwords owned by the user
     */
    public GliderUser(String id, String token, String name, String surname, String email, String password, String language,
                      List<DeviceUserSession> devices, List<Password> passwords) {
        super(id, token, name, surname, email, password, null, language);
        this.devices = devices;
        this.passwords = passwords;
    }

    /**
     * Method used to get the {@link #devices} instance
     *
     * @return the {@link #devices} instance as {@link List} of {@link ConnectedDevice}
     */
    @JsonIgnore
    public List<ConnectedDevice> getDevices() {
        return devices.stream().map(DeviceUserSession::getDevice).toList();
    }

    /**
     * Method used to get the {@link #passwords} instance
     *
     * @return the {@link #passwords} instance as {@link List} of {@link Password}
     */
    public List<Password> getPasswords() {
        return passwords;
    }

    /**
     * Method used to check whether the specified device belongs to the user
     *
     * @param deviceId The identifier of the device to check
     *
     * @return whether the specified device belongs to the user as {@code boolean}
     */
    @JsonIgnore
    public boolean deviceBelongsToMe(String deviceId) {
        for (DeviceUserSession deviceSession : devices) {
            ConnectedDevice device = deviceSession.getDevice();
            if (device.getId().equals(deviceId))
                return true;
        }
        return false;
    }

    /**
     * Method used to check whether the specified password belongs to the user
     *
     * @param passwordId The identifier of the password to check
     *
     * @return whether the specified password belongs to the user as {@code boolean}
     */
    @JsonIgnore
    public boolean passwordBelongsToMe(String passwordId) {
        for (Password password : passwords)
            if (password.getId().equals(passwordId))
                return true;
        return false;
    }

}
