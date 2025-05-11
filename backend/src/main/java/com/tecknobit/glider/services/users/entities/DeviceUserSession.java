package com.tecknobit.glider.services.users.entities;

import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;

import static com.tecknobit.equinoxcore.helpers.CommonKeysKt.USER_IDENTIFIER_KEY;
import static com.tecknobit.glidercore.ConstantsKt.*;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

/**
 * The {@code DeviceUserSession} class is useful to represent the relationship between {@link ConnectedDevice} and
 * {@link GliderUser}
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = USER_DEVICES_KEY)
public class DeviceUserSession extends EquinoxItem {

    /**
     * {@code user} the user owner of the {@link #device}
     */
    @ManyToOne
    @JoinColumn(name = USER_IDENTIFIER_KEY)
    @OnDelete(action = CASCADE)
    private GliderUser user;

    /**
     * {@code device} the device details
     */
    @ManyToOne
    @JoinColumn(name = DEVICE_IDENTIFIER_KEY)
    @OnDelete(action = CASCADE)
    private final ConnectedDevice device;

    /**
     * {@code lastLogin} the last login of the specific session
     */
    @Column(
            name = LAST_LOGIN_KEY,
            columnDefinition = "BIGINT(20) DEFAULT -1"
    )
    private final long lastLogin;

    /**
     * Constructor to init the {@link DeviceUserSession} class
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public DeviceUserSession() {
        this(null, null, -1);
    }

    /**
     * Constructor to init the {@link DeviceUserSession} class
     *
     * @param sessionId The identifier of the session
     * @param device    The device details
     * @param lastLogin The last login of the specific session
     */
    public DeviceUserSession(String sessionId, ConnectedDevice device, long lastLogin) {
        super(sessionId);
        this.device = device;
        this.lastLogin = lastLogin;
    }

    /**
     * Method used to get the {@link #device} instance
     *
     * @return the {@link #device} instance as {@link ConnectedDevice}
     */
    public ConnectedDevice getDevice() {
        return device;
    }

    /**
     * Method used to get the {@link #lastLogin} instance
     *
     * @return the {@link #lastLogin} instance as {@code long}
     */
    public long getLastLogin() {
        return lastLogin;
    }

}
