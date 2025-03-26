package com.tecknobit.glider.services.users.entities;

import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import static com.tecknobit.glidercore.ConstantsKt.*;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = USER_DEVICES_KEY)
public class DeviceUserSession extends EquinoxItem {

    @ManyToOne
    @JoinColumn(name = USER_IDENTIFIER_KEY)
    @OnDelete(action = CASCADE)
    private GliderUser user;

    @ManyToOne
    @JoinColumn(name = DEVICE_IDENTIFIER_KEY)
    @OnDelete(action = CASCADE)
    private final ConnectedDevice device;

    @Column(
            name = LAST_LOGIN_KEY,
            columnDefinition = "BIGINT(20) DEFAULT -1"
    )
    private final long lastLogin;

    public DeviceUserSession() {
        this(null, null, -1);
    }

    public DeviceUserSession(String sessionId, ConnectedDevice device, long lastLogin) {
        super(sessionId);
        this.device = device;
        this.lastLogin = lastLogin;
    }

    public ConnectedDevice getDevice() {
        return device;
    }

    public long getLastLogin() {
        return lastLogin;
    }

}
