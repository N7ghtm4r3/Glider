package com.tecknobit.glider.services.users.entities;

import com.fasterxml.jackson.annotation.JsonGetter;
import com.tecknobit.equinoxbackend.environment.services.builtin.entity.EquinoxItem;
import com.tecknobit.glidercore.enums.ConnectedDeviceType;
import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.json.JSONObject;

import java.util.List;

import static com.tecknobit.glidercore.ConstantsKt.*;
import static org.hibernate.annotations.OnDeleteAction.CASCADE;

@Entity
@Table(name = DEVICES_KEY)
public class ConnectedDevice extends EquinoxItem {

    @Column
    private final String brand;

    @Column
    private final String model;

    @Column
    private final String browser;

    @Column(
            name = LAST_LOGIN_KEY,
            columnDefinition = "BIGINT(20) DEFAULT -1"
    )
    private final long lastLogin;

    @Enumerated(EnumType.STRING)
    private final ConnectedDeviceType type;

    @ManyToMany(
            mappedBy = DEVICES_KEY
    )
    @OnDelete(action = CASCADE)
    private List<GliderUser> users;

    public ConnectedDevice() {
        this(null, null, null, null, -1, null);
    }

    public ConnectedDevice(String id, String brand, String model, String browser, long lastLogin,
                           ConnectedDeviceType type) {
        super(id);
        this.brand = brand;
        this.model = model;
        this.browser = browser;
        this.lastLogin = lastLogin;
        this.type = type;
    }

    public ConnectedDevice(JSONObject jDevice) {
        super(jDevice);
        brand = hItem.getString(BRAND_KEY);
        model = hItem.getString(MODEL_KEY);
        browser = hItem.getString(BROWSER_KEY);
        lastLogin = System.currentTimeMillis();
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

    @JsonGetter(LAST_LOGIN_KEY)
    public long getLastLogin() {
        return lastLogin;
    }

    public ConnectedDeviceType getType() {
        return type;
    }

}
