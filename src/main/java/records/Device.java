package records;

import com.tecknobit.apimanager.formatters.TimeFormatter;
import org.json.JSONObject;

/**
 * The {@link Device} is class useful to store all the information for a {@code Glider}'s device
 * allowing the correct workflow
 *
 * @author Tecknobit - N7ghtm4r3
 **/
public class Device {

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
        this(name, ipAddress, loginDate, lastActivity, type, false);
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
        this.name = name;
        this.ipAddress = ipAddress;
        this.loginDate = loginDate;
        this.lastActivity = lastActivity;
        this.type = type;
        this.blacklisted = blacklisted;
    }

    /**
     * Method to get {@link #name} instance <br>
     * Any params required
     *
     * @return {@link #name} instance as {@link String}
     **/
    public String getName() {
        return name;
    }

    /**
     * Method to get {@link #ipAddress} instance <br>
     * Any params required
     *
     * @return {@link #ipAddress} instance as {@link String}
     **/
    public String getIpAddress() {
        return ipAddress;
    }

    /**
     * Method to get {@link #loginDate} instance <br>
     * Any params required
     *
     * @return {@link #loginDate} instance as {@link String}
     **/
    public String getLoginDate() {
        return loginDate;
    }

    /**
     * Method to get {@link #loginDate} instance <br>
     * Any params required
     *
     * @return {@link #loginDate} instance as long
     **/
    public long getLoginDateTimestamp() {
        return TimeFormatter.getDateTimestamp(loginDate);
    }

    /**
     * Method to get {@link #lastActivity} instance <br>
     * Any params required
     *
     * @return {@link #lastActivity} instance as {@link String}
     **/
    public String getLastActivity() {
        return lastActivity;
    }

    /**
     * Method to get {@link #lastActivity} instance <br>
     * Any params required
     *
     * @return {@link #lastActivity} instance as long
     **/
    public long getLastActivityTimestamp() {
        return TimeFormatter.getDateTimestamp(lastActivity);
    }

    /**
     * Method to get {@link #type} instance <br>
     * Any params required
     *
     * @return {@link #type} instance as {@link Type}
     **/
    public Type getType() {
        return type;
    }

    /**
     * Method to get {@link #blacklisted} instance <br>
     * Any params required
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
     * Returns a string representation of the object <br>
     * Any params required
     *
     * @return a string representation of the object as {@link String}
     */
    @Override
    public String toString() {
        return new JSONObject(this).toString();
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
