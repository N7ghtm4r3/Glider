package com.tecknobit.glider.services.users.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.annotations.EmptyConstructor;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glidercore.enums.ConnectedDeviceType;
import jakarta.persistence.*;
import org.json.JSONObject;

import java.util.List;

import static com.tecknobit.glidercore.ConstantsKt.*;

/**
 * The {@code ConnectedDevice} class is useful to represent the details about a device connected to a session
 *
 * @author N7ghtm4r3 - Tecknobit
 * @see EquinoxItem
 */
@Entity
@Table(name = DEVICES_KEY)
public class ConnectedDevice extends EquinoxItem {

    /**
     * {@code brand} the brand of the device
     */
    @Column
    private final String brand;

    /**
     * {@code model} the model of the device
     */
    @Column
    private final String model;

    /**
     * {@code browser} the browser of the device
     */
    @Column
    private final String browser;

    /**
     * {@code type} the type of the device
     */
    @Enumerated(EnumType.STRING)
    private final ConnectedDeviceType type;

    /**
     * {@code sessions} the sessions where the device is connected
     */
    @OneToMany(
            mappedBy = DEVICE_KEY,
            cascade = CascadeType.ALL
    )
    private List<DeviceUserSession> sessions;

    /**
     * {@code lastLogin} the last login of a specific session
     */
    @Transient
    private long lastLogin;

    /**
     * Constructor to init the {@link ConnectedDevice} class
     *
     * @apiNote empty constructor required
     */
    @EmptyConstructor
    public ConnectedDevice() {
        this(null, null, null, null, null);
    }

    /**
     * Constructor to init the {@link ConnectedDevice} class
     *
     * @param id      The identifier of the device
     * @param brand   The brand of the device
     * @param model   The model of the device
     * @param browser The browser of the device
     * @param type    The type of the device
     */
    public ConnectedDevice(String id, String brand, String model, String browser, ConnectedDeviceType type) {
        super(id);
        this.brand = brand;
        this.model = model;
        this.browser = browser;
        this.type = type;
    }

    /**
     * Constructor to init the {@link ConnectedDevice} class
     *
     * @param jDevice The JSON details of the device
     */
    public ConnectedDevice(JSONObject jDevice) {
        super(jDevice);
        brand = hItem.getString(BRAND_KEY);
        model = hItem.getString(MODEL_KEY);
        browser = hItem.getString(BROWSER_KEY);
        type = ConnectedDeviceType.valueOf(hItem.getString(TYPE_KEY, ConnectedDeviceType.MOBILE.name()));
    }

    /**
     * Method used to get the {@link #brand} instance
     *
     * @return the {@link #brand} instance as {@link String}
     */
    public String getBrand() {
        return brand;
    }

    /**
     * Method used to get the {@link #model} instance
     *
     * @return the {@link #model} instance as {@link String}
     */
    public String getModel() {
        return model;
    }

    /**
     * Method used to get the {@link #browser} instance
     *
     * @return the {@link #browser} instance as {@link String}
     */
    public String getBrowser() {
        return browser;
    }

    /**
     * Method used to get the {@link #type} instance
     *
     * @return the {@link #type} instance as {@link ConnectedDeviceType}
     */
    public ConnectedDeviceType getType() {
        return type;
    }

    /**
     * Method used to get the {@link #lastLogin} instance
     *
     * @return the {@link #lastLogin} instance as {@code long}
     */
    @JsonGetter(LAST_LOGIN_KEY)
    public long getLastLogin() {
        return lastLogin;
    }

    /**
     * Method used to set the {@link #lastLogin} instance
     *
     * @param lastLogin The last login of a specific session
     */
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

}
