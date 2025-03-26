package com.tecknobit.glider.services.users.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.tecknobit.equinoxbackend.environment.services.users.entity.EquinoxUser;
import jakarta.persistence.*;

import java.util.List;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.IDENTIFIER_KEY;
import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.USERS_KEY;
import static com.tecknobit.glidercore.ConstantsKt.*;

@Entity
@Table(name = USERS_KEY)
public class GliderUser extends EquinoxUser {

    @ManyToMany
    @JoinTable(
            name = USER_DEVICES_KEY,
            joinColumns = @JoinColumn(
                    name = USER_IDENTIFIER_KEY,
                    referencedColumnName = IDENTIFIER_KEY
            ),
            inverseJoinColumns = @JoinColumn(
                    name = DEVICE_IDENTIFIER_KEY,
                    referencedColumnName = IDENTIFIER_KEY
            )
    )
    private final List<ConnectedDevice> devices;

    public GliderUser(String id, String token, String name, String surname, String email, String password, String language,
                      List<ConnectedDevice> devices) {
        super(id, token, name, surname, email, password, null, language);
        this.devices = devices;
    }

    @JsonIgnore
    public List<ConnectedDevice> getDevices() {
        return devices;
    }

}
