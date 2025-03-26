package com.tecknobit.glider.services.users.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glidercore.enums.ConnectedDeviceType;
import jakarta.persistence.*;
import org.json.JSONObject;

import java.util.List;

import static com.tecknobit.glidercore.ConstantsKt.*;

@Entity
@Table(name = DEVICES_KEY)
public class ConnectedDevice extends EquinoxItem {

    @Column
    private final String brand;

    @Column
    private final String model;

    @Column
    private final String browser;

    @Enumerated(EnumType.STRING)
    private final ConnectedDeviceType type;

    @OneToMany(
            mappedBy = DEVICE_KEY,
            cascade = CascadeType.ALL
    )
    private List<DeviceUserSession> sessions;

    @Transient
    private long lastLogin;

    public ConnectedDevice() {
        this(null, null, null, null, null);
    }

    public ConnectedDevice(String id, String brand, String model, String browser, ConnectedDeviceType type) {
        super(id);
        this.brand = brand;
        this.model = model;
        this.browser = browser;
        this.type = type;
    }

    public ConnectedDevice(JSONObject jDevice) {
        super(jDevice);
        brand = hItem.getString(BRAND_KEY);
        model = hItem.getString(MODEL_KEY);
        browser = hItem.getString(BROWSER_KEY);
        type = ConnectedDeviceType.valueOf(hItem.getString(DEVICE_TYPE_KEY, ConnectedDeviceType.MOBILE.name()));
    }

    public String getBrand() {
        return brand;
    }

    public String getModel() {
        return model;
    }

    public String getBrowser() {
        return browser;
    }

    public ConnectedDeviceType getType() {
        return type;
    }

    @JsonGetter(LAST_LOGIN_KEY)
    public long getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }

}
