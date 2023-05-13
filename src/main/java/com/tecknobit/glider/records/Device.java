package com.tecknobit.glider.records;

import com.tecknobit.apimanager.formatters.TimeFormatter;
import org.json.JSONObject;

/**
 * The {@link Device} is class useful to store all the information for a {@code Glider}'s device
 * allowing the correct workflow
 *
 * @author Tecknobit - N7ghtm4r3
 * @see GliderRecord
 **/
public class Device extends GliderRecord {

    /**
     * {@code DeviceKeys} list of available keys for the device
     **/
    public enum DeviceKeys {

        /**
         * {@code name} key of the device
         **/
        name,

        /**
         * {@code ipAddress} key of the device
         **/
        ipAddress,

        /**
         * {@code loginDate} key of the device
         **/
        loginDate,

        /**
         * {@code lastActivity} key of the device
         **/
        lastActivity,

        /**
         * {@code type} key of the device
         **/
        type,

        /**
         * {@code blacklisted} key of the device
         **/
        blacklisted,

        /**
         * {@code targetDevice} target device key
         **/
        targetDevice

    }

    /**
     * {@code name} of the device
     **/
    private final String name;

    /**
     * {@code ipAddress} ip address of the device
     **/
    private final String ipAddress;

    /**
     * {@code loginDate} date of the device
     **/
    private final String loginDate;

    /**
     * {@code lastActivity} last activity of the device
     **/
    private final String lastActivity;

    /**
     * {@code type} of the device
     **/
    private final Type type;

    /**
     * {@code blacklisted} whether the device has been blacklisted
     **/
    private boolean blacklisted;

    /**
     * Constructor to init {@link Device} object
     *
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @param loginDate:    loginDate date of the device
     * @param lastActivity: last activity of the device
     * @param type:         type of the devices
     **/
    public Device(String name, String ipAddress, String loginDate, String lastActivity, Type type) {
        this(null, name, ipAddress, loginDate, lastActivity, type, false);
    }

    /**
     * Constructor to init {@link Device} object
     *
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @param loginDate:    loginDate date of the device
     * @param lastActivity: last activity of the device
     * @param type:         type of the devices
     * @param blacklisted:  whether the device has been blacklisted
     **/
    public Device(String name, String ipAddress, String loginDate, String lastActivity, Type type,
                  boolean blacklisted) {
        this(null, name, ipAddress, loginDate, lastActivity, type, blacklisted);
    }

    /**
     * Constructor to init {@link Device} object
     *
     * @param session: session value
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @param loginDate:    loginDate date of the device
     * @param lastActivity: last activity of the device
     * @param type:         type of the devices
     **/
    public Device(Session session, String name, String ipAddress, String loginDate, String lastActivity, Type type) {
        this(session, name, ipAddress, loginDate, lastActivity, type, false);
    }

    /**
     * Constructor to init {@link Device} object
     *
     * @param session: session value
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @param loginDate:    loginDate date of the device
     * @param lastActivity: last activity of the device
     * @param type:         type of the devices
     * @param blacklisted:  whether the device has been blacklisted
     **/
    public Device(Session session, String name, String ipAddress, String loginDate, String lastActivity, Type type,
                  boolean blacklisted) {
        super(session);
        this.name = name;
        this.ipAddress = ipAddress;
        this.loginDate = loginDate;
        this.lastActivity = lastActivity;
        this.type = type;
        this.blacklisted = blacklisted;
    }

    /**
     * Constructor to init {@link Device} object
     *
     * @param jDevice : device details as {@link JSONObject}
     **/
    public Device(JSONObject jDevice) {
        super(jDevice);
        name = hRecord.getString(DeviceKeys.name.name());
        ipAddress = hRecord.getString(DeviceKeys.ipAddress.name());
        loginDate = hRecord.getString(DeviceKeys.loginDate.name());
        lastActivity = hRecord.getString(DeviceKeys.lastActivity.name());
        type = Type.valueOf(hRecord.getString(DeviceKeys.type.name(), Type.MOBILE.name()));
        blacklisted = hRecord.getBoolean(DeviceKeys.blacklisted.name());
    }

    /**
     * Method to get {@link #name} instance <br>
     * No-any params required
     *
     * @return {@link #name} instance as {@link String}
     **/
    public String getName() {
        return name;
    }

    /**
     * Method to get {@link #ipAddress} instance <br>
     * No-any params required
     *
     * @return {@link #ipAddress} instance as {@link String}
     **/
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Method to get {@link #loginDate} instance <br>
     * No-any params required
     *
     * @return {@link #loginDate} instance as {@link String}
     **/
    public String getLoginDate() {
        return loginDate;
    }

    /**
     * Method to get {@link #loginDate} instance <br>
     * No-any params required
     *
     * @return {@link #loginDate} instance as long
     **/
    public long getLoginDateTimestamp() {
        return TimeFormatter.getDateTimestamp(loginDate);
    }

    /**
     * Method to get {@link #lastActivity} instance <br>
     * No-any params required
     *
     * @return {@link #lastActivity} instance as {@link String}
     **/
    public String getLastActivity() {
        return lastActivity;
    }

    /**
     * Method to get {@link #lastActivity} instance <br>
     * No-any params required
     *
     * @return {@link #lastActivity} instance as long
     **/
    public long getLastActivityTimestamp() {
        return TimeFormatter.getDateTimestamp(lastActivity);
    }

    /**
     * Method to get {@link #type} instance <br>
     * No-any params required
     *
     * @return {@link #type} instance as {@link Type}
     **/
    public Type getType() {
        return type;
    }

    /**
     * Method to get {@link #blacklisted} instance <br>
     * No-any params required
     *
     * @return {@link #blacklisted} instance as boolean
     **/
    public boolean isBlacklisted() {
        return blacklisted;
    }

    /**
     * Method to set {@link #blacklisted} instance on {@link "true"}
     **/
    public void blacklist() {
        blacklisted = true;
    }

    /**
     * Method to set {@link #blacklisted} instance on {@link "false"}
     **/
    public void unblacklist() {
        blacklisted = false;
    }

    /**
     * {@code Type} list of available types for a {@link Device}
     **/
    public enum Type {

        /**
         * {@code "DESKTOP"} device type
         **/
        DESKTOP,

        /**
         * {@code "MOBILE"} device type
         **/
        MOBILE

    }

}
