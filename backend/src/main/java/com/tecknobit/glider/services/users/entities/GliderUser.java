package com.tecknobit.glider.services.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import com.tecknobit.glider.services.passwords.entities.Password;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.util.List;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.USERS_KEY;
import static com.tecknobit.glidercore.ConstantsKt.USER_KEY;

@Entity
@Table(name = USERS_KEY)
public class GliderUser extends EquinoxUser {

    @OneToMany(
            mappedBy = USER_KEY,
            cascade = CascadeType.ALL
    )
    private final List<DeviceUserSession> devices;

    @OneToMany(
            mappedBy = USER_KEY,
            cascade = CascadeType.ALL
    )
    private final List<Password> passwords;

    public GliderUser() {
        this(null, null, null, null, null, null, null, List.of(),
                List.of());
    }

    public GliderUser(String id, String token, String name, String surname, String email, String password, String language,
                      List<DeviceUserSession> devices, List<Password> passwords) {
        super(id, token, name, surname, email, password, null, language);
        this.devices = devices;
        this.passwords = passwords;
    }

    @JsonIgnore
    public List<ConnectedDevice> getDevices() {
        return devices.stream().map(DeviceUserSession::getDevice).toList();
    }

    @JsonIgnore
    public boolean deviceBelongsToMe(String deviceId) {
        for (DeviceUserSession deviceSession : devices) {
            ConnectedDevice device = deviceSession.getDevice();
            if (device.getId().equals(deviceId))
                return true;
        }
        return false;
    }

    public List<Password> getPasswords() {
        return passwords;
    }

}
