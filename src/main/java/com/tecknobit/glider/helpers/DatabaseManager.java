package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.annotations.Wrapper;
import com.tecknobit.apimanager.apis.encryption.aes.AESClientCipher;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Device.DeviceKeys;
import com.tecknobit.glider.records.Device.DevicePermission;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Password.PasswordKeys;
import com.tecknobit.glider.records.Session;
import com.tecknobit.glider.records.Session.SessionKeys;
import org.json.JSONArray;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import static com.tecknobit.apimanager.apis.encryption.BaseCipher.Algorithm.CBC_ALGORITHM;
import static com.tecknobit.apimanager.formatters.TimeFormatter.getStringDate;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.*;
import static com.tecknobit.glider.records.Device.DeviceKeys.*;
import static com.tecknobit.glider.records.Device.Type;
import static com.tecknobit.glider.records.Password.PasswordKeys.*;
import static com.tecknobit.glider.records.Password.Status;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;
import static com.tecknobit.glider.records.Password.Status.DELETED;
import static com.tecknobit.glider.records.Password.fetchScopes;
import static com.tecknobit.glider.records.Session.SessionKeys.*;
import static java.lang.Boolean.parseBoolean;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import static java.lang.System.currentTimeMillis;

/**
 * The {@link DatabaseManager} is class useful to manage the {@code SQL} database where are stored all the
 * information <br>
 * This will create, or will read, a database created by a custom path choose by you and from which the {@code Glider}'s
 * library will get the data for its workflow
 *
 * @author Tecknobit - N7ghtm4r3
 **/
public class DatabaseManager {

    /**
     * {@code Table} list of table for the database
     */
    public enum Table {

        /**
         * {@code "sessions"} table where are stored all the session created
         */
        sessions,

        /**
         * {@code "devices"} table where are stored all the devices connected to the {@link #sessions}
         */
        devices,

        /**
         * {@code "passwords"} table where are stored all the passwords of a session of the {@link #sessions}
         */
        passwords

    }

    /**
     * {@code connection} instance to manage the database's connection
     */
    private final Connection connection;

    /**
     * {@code databasePath} value of the database path
     **/
    private final String databasePath;

    /**
     * Constructor to init {@link DatabaseManager} object
     *
     * @param databasePath: path where create the database, if it exists will be only read and not recreated
     * @apiNote will be set the {@link #connection} instance and created the tables if they don't exist
     **/
    public DatabaseManager(String databasePath) throws IOException, SQLException {
        if (!databasePath.endsWith(".db"))
            databasePath += ".db";
        File database = new File(databasePath);
        this.databasePath = databasePath;
        boolean dbCreated = true;
        if(!database.exists())
            dbCreated = database.createNewFile();
        if(dbCreated) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            Statement statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("CREATE TABLE IF NOT EXISTS " + sessions + " (\n" +
                    "token VARCHAR(64) PRIMARY KEY, \n" +
                    "ivSpec VARCHAR(44) NOT NULL, \n" +
                    "secretKey VARCHAR(64) NOT NULL, \n" +
                    "password VARCHAR(24) NOT NULL, \n" +
                    "hostAddress VARCHAR(24) NOT NULL, \n" +
                    "hostPort VARCHAR(24) NOT NULL, \n" +
                    "singleUseMode VARCHAR(24) NOT NULL, \n" +
                    "QRCodeLoginEnabled VARCHAR(24) NOT NULL, \n" +
                    "runInLocalhost VARCHAR(24) NOT NULL);"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS " + devices + "(\n" +
                    "token VARCHAR(64) NOT NULL, \n" +
                    "name VARCHAR(24) NOT NULL, \n" +
                    "ipAddress VARCHAR(24) NOT NULL, \n" +
                    "loginDate VARCHAR(24) NOT NULL, \n" +
                    "lastActivity VARCHAR(24) NOT NULL, \n" +
                    "type VARCHAR(24) NOT NULL, \n" +
                    "blacklisted VARCHAR(24) NOT NULL, \n" +
                    "permission VARCHAR(16) DEFAULT SIMPLE_USER, \n" +
                    "PRIMARY KEY(token, name), \n" +
                    "FOREIGN KEY(token) REFERENCES " + sessions + "(token) ON DELETE CASCADE);"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS " + passwords + "(\n" +
                    "token VARCHAR(64) NOT NULL, \n" +
                    "tail VARCHAR(24) NOT NULL, \n" +
                    "scopes VARCHAR(24) NOT NULL, \n" +
                    "password VARCHAR(24) UNIQUE NOT NULL, \n" +
                    "status VARCHAR(24) NOT NULL, \n" +
                    "PRIMARY KEY(token, tail), \n" +
                    "FOREIGN KEY(token) REFERENCES " + sessions + "(token) ON DELETE CASCADE);"
            );
        } else
            throw new IOException("Cannot create a database file, check the inserted path");
    }

    /**
     * Method to insert a new session in the {@link Table#sessions} table
     * @param session: session to insert
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void insertNewSession(Session session) throws Exception {
        insertNewSession(session.getToken(), session.getIvSpec(), session.getSecretKey(), session.getSessionPassword(),
                session.getHostAddress(), session.getHostPort(), session.isSingleUseMode(),
                session.isQRCodeLoginEnabled(), session.runInLocalhost());
    }

    /**
     * Method to insert a new session in the {@link Table#sessions} table
     *
     * @param token: session token value
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     * @param password: password of the session
     * @param hostAddress:   hostAddress address of the session
     * @param hostPort: hostAddress hostPort of the session
     * @param singleUseMode:   whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled:   whether the session allows login by QR-CODE method
     * @param runInLocalhost: whether the session run only in localhost
     * @throws Exception when an error occurred
     **/
    public void insertNewSession(String token, String ivSpec, String secretKey, String password, String hostAddress,
                                 int hostPort, boolean singleUseMode, boolean QRCodeLoginEnabled,
                                 boolean runInLocalhost) throws Exception {
        connection.prepareStatement("INSERT INTO " + sessions + "(token, ivSpec, secretKey, password, hostAddress,"
                + " hostPort, singleUseMode, QRCodeLoginEnabled, runInLocalhost)" + " VALUES('" + encrypt(ivSpec,
                secretKey, token) + "','" + encrypt(ivSpec, secretKey, ivSpec) + "','" + encrypt(ivSpec, secretKey,
                secretKey) + "','" + encrypt(ivSpec, secretKey, password) + "','" + encrypt(ivSpec, secretKey,
                hostAddress) + "','" + encrypt(ivSpec, secretKey, hostPort) + "','" + encrypt(ivSpec, secretKey,
                singleUseMode) + "','" + encrypt(ivSpec, secretKey, QRCodeLoginEnabled) + "','" + encrypt(ivSpec,
                secretKey, runInLocalhost) + "')").executeUpdate();
    }

    /**
     * Method to get a {@link Session} from the database
     * @param token: session to fetch
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     *
     * @return session as {@link Session}
     * @throws SQLException when an error occurred
     **/
    public Session getSession(String token, String ivSpec, String secretKey) throws Exception {
        ResultSet rSession = fetchRecord("SELECT * FROM " + sessions + " WHERE token='" + encrypt(ivSpec, secretKey,
                token) + "'");
        if(rSession.next()) {
            Session session = new Session(token, decrypt(ivSpec, secretKey, rSession.getString(SessionKeys.ivSpec.name())),
                    decrypt(ivSpec, secretKey, rSession.getString(SessionKeys.secretKey.name())),
                    decrypt(ivSpec, secretKey, rSession.getString(password.name())),
                    decrypt(ivSpec, secretKey, rSession.getString(hostAddress.name())),
                    parseInt(decrypt(ivSpec, secretKey, rSession.getString(hostPort.name()))),
                    parseBoolean(decrypt(ivSpec, secretKey, rSession.getString(singleUseMode.name()))),
                    parseBoolean(decrypt(ivSpec, secretKey, rSession.getString(QRCodeLoginEnabled.name()))),
                    parseBoolean(decrypt(ivSpec, secretKey, rSession.getString(runInLocalhost.name()))));
            rSession.close();
            return session;
        }
        return null;
    }

    /**
     * Method to remove a session from the {@link Table#sessions} table
     *
     * @param session: session to delete
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void deleteSession(Session session) throws Exception {
        deleteSession(session.getToken(), session.getIvSpec(), session.getSecretKey());
    }

    /**
     * Method to remove a session from the {@link Table#sessions} table
     *
     * @param token: session to delete
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     * @throws Exception when an error occurred
     **/
    public void deleteSession(String token, String ivSpec, String secretKey) throws Exception {
        connection.prepareStatement("DELETE FROM " + sessions + " WHERE token='" + encrypt(ivSpec, secretKey,
                token) + "'").executeUpdate();
    }

    /**
     * Method to insert a new device in the {@link Table#devices} table
     * @param device: device to insert
     * @apiNote the device will link a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void insertNewDevice(Device device) throws Exception {
        insertNewDevice(device.getSession(), device.getName(), device.getIpAddress(), device.getLoginDateTimestamp(),
                device.getType(), device.getPermission());
    }

    /**
     * Method to insert a new device in the {@link Table#devices} table
     *
     * @param session     : session where device is connected
     * @param name        :         name of the device
     * @param ipAddress   :    ip address of the device
     * @param loginDate   :    loginDate date of the device
     * @param type        :         type of the devices
     * @param permission: permission of the device
     * @throws Exception when an error occurred
     * @apiNote the device will link with a one {@link Session} inserted in the {@link Table#sessions} table
     **/
    public void insertNewDevice(Session session, String name, String ipAddress, long loginDate, Type type,
                                DevicePermission permission) throws Exception {
        String sLoginDate = encrypt(session, loginDate);
        connection.prepareStatement("INSERT INTO " + devices + "(token, name, ipAddress, loginDate, lastActivity,"
                + "type, blacklisted, permission) VALUES('" + encrypt(session, session.getToken())
                + "','" + encrypt(session, name) + "','" + encrypt(session, ipAddress) + "','" + sLoginDate
                + "','" + sLoginDate + "','" + encrypt(session, type) + "','" + encrypt(session, false)
                + "','" + encrypt(session, permission) + "')").executeUpdate();
    }

    /**
     * Method to get a {@link Device} from the database
     *
     * @param session: session linked to the device to fetch
     * @param name:    name of the device
     * @return device as {@link Device}
     * @throws Exception when an error occurred
     **/
    public Device getDevice(Session session, String name) throws Exception {
        ResultSet rDevice = fetchRecord("SELECT * FROM " + devices + " WHERE token='" + encrypt(session,
                session.getToken()) + "' AND name='" + encrypt(session, name) + "'");
        if (rDevice.next()) {
            Device device = new Device(session,
                    decrypt(session, rDevice.getString(DeviceKeys.name.name())),
                    decrypt(session, rDevice.getString(DeviceKeys.ipAddress.name())),
                    getStringDate(parseLong(decrypt(session, rDevice.getString(loginDate.name())))),
                    getStringDate(parseLong(decrypt(session, rDevice.getString(lastActivity.name())))),
                    Type.valueOf(decrypt(session, rDevice.getString(type.name()))),
                    parseBoolean(decrypt(session, rDevice.getString(blacklisted.name()))),
                    DevicePermission.valueOf(decrypt(session, rDevice.getString(permission.name()))));
            rDevice.close();
            return device;
        }
        return null;
    }

    /**
     * Method to get the older {@link Device} by its permission from the database
     *
     * @param session:    session linked to the device to fetch
     * @param permission: permission target
     * @return older device as {@link Device}
     * @throws Exception when an error occurred
     */
    @Wrapper
    public Device getOlderDevice(Session session, DevicePermission permission) throws Exception {
        return getOlderDevice(session, permission, null);
    }

    /**
     * Method to get the older {@link Device} by its permission from the database
     *
     * @param session:       session linked to the devices to fetch
     * @param permission:    permission target
     * @param excludeDevice: the device to exclude
     * @return older device as {@link Device}
     * @throws Exception when an error occurred
     */
    public Device getOlderDevice(Session session, DevicePermission permission, String excludeDevice) throws Exception {
        long olderDeviceLoginDate = System.currentTimeMillis();
        Device olderDevice = null;
        for (Device device : getDevices(session, false, excludeDevice, permission)) {
            long loginDate = device.getLoginDateTimestamp();
            if (loginDate < olderDeviceLoginDate) {
                olderDeviceLoginDate = loginDate;
                olderDevice = device;
            }
        }
        return olderDevice;
    }

    /**
     * Method to get a list of {@link Device} from the database
     *
     * @param session:       session linked to the devices to fetch
     * @param insertSession: whether insert the session, if {@code "false"} the session will be set as null
     * @return devices list as {@link ArrayList} of {@link Device}
     * @throws Exception when an error occurred
     */
    @Wrapper
    public ArrayList<Device> getDevices(Session session, boolean insertSession) throws Exception {
        return getDevices(session, insertSession, (String) null);
    }

    /**
     * Method to get a list of {@link Device} from the database
     *
     * @param session:       session linked to the devices to fetch
     * @param insertSession: whether insert the session, if {@code "false"} the session will be set as null
     * @param excludeDevice: the device to exclude
     * @return devices list as {@link ArrayList} of {@link Device}
     * @throws Exception when an error occurred
     */
    public ArrayList<Device> getDevices(Session session, boolean insertSession, String excludeDevice) throws Exception {
        return getDevices(session, insertSession, null, excludeDevice);
    }

    /**
     * Method to get a list of {@link Device} filtered by them {@link DevicePermission} from the database
     *
     * @param session:       session linked to the devices to fetch
     * @param insertSession: whether insert the session, if {@code "false"} the session will be set as null
     * @param permission:    the filter permission
     * @return filtered devices list as {@link ArrayList} of {@link Device}
     * @throws Exception when an error occurred
     */
    @Wrapper
    public ArrayList<Device> getDevices(Session session, boolean insertSession, DevicePermission permission) throws Exception {
        return getDevices(session, insertSession, null, permission);
    }

    /**
     * Method to get a list of {@link Device} filtered by them {@link DevicePermission} from the database
     *
     * @param session:       session linked to the devices to fetch
     * @param insertSession: whether insert the session, if {@code "false"} the session will be set as null
     * @param permission:    the filter permission
     * @param excludeDevice: the device to exclude
     * @return filtered devices list as {@link ArrayList} of {@link Device}
     * @throws Exception when an error occurred
     */
    public ArrayList<Device> getDevices(Session session, boolean insertSession, String excludeDevice,
                                        DevicePermission permission) throws Exception {
        return getDevices(session, insertSession, permission, excludeDevice);
    }

    /**
     * Method to get a list of {@link Device} from the database
     *
     * @param session:       session linked to the devices to fetch
     * @param insertSession: whether insert the session, if {@code "false"} the session will be set as null
     * @param permission:    the filter permission
     * @param excludeDevice: the device to exclude
     * @return filtered devices list as {@link ArrayList} of {@link Device}
     * @throws Exception when an error occurred
     */
    private ArrayList<Device> getDevices(Session session, boolean insertSession, DevicePermission permission,
                                         String excludeDevice) throws Exception {
        String query = "SELECT * FROM " + devices + " WHERE token='" + encrypt(session, session.getToken()) + "'";
        if (permission != null)
            query += " AND permission='" + encrypt(session, permission) + "'";
        ResultSet rDevices = fetchRecord(query);
        ArrayList<Device> devices = new ArrayList<>();
        while (rDevices.next()) {
            Session iSession = null;
            if (insertSession)
                iSession = session;
            String vDeviceName = decrypt(session, rDevices.getString(DeviceKeys.name.name()));
            if (!vDeviceName.equals(excludeDevice)) {
                devices.add(new Device(iSession,
                        vDeviceName,
                        decrypt(session, rDevices.getString(ipAddress.name())),
                        getStringDate(parseLong(decrypt(session, rDevices.getString(loginDate.name())))),
                        getStringDate(parseLong(decrypt(session, rDevices.getString(lastActivity.name())))),
                        Type.valueOf(decrypt(session, rDevices.getString(type.name()))),
                        parseBoolean(decrypt(session, rDevices.getString(blacklisted.name()))),
                        DevicePermission.valueOf(decrypt(session, rDevices.getString(DeviceKeys.permission.name())))));
            }
        }
        rDevices.close();
        return devices;
    }

    /**
     * Method to update the last activity of a {@link Device}
     *
     * @param device:    device to update its last activity
     * @param ipAddress: the ip address to update
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void updateLastActivity(Device device, String ipAddress) throws Exception {
        updateLastActivity(device.getSession(), device.getName(), ipAddress);
    }

    /**
     * Method to update the last activity of a {@link Device}
     *
     * @param session: session linked to the device
     * @param name:         name of the device
     * @param ipAddress: the ip address to update
     * @throws SQLException when an error occurred
     **/
    public void updateLastActivity(Session session, String name, String ipAddress) throws Exception {
        connection.prepareStatement("UPDATE " + devices + " SET lastActivity='" + encrypt(session,
                currentTimeMillis()) + "' , ipAddress='" + encrypt(session, ipAddress) + "' WHERE token='"
                + encrypt(session, session.getToken()) + "' AND name='" + encrypt(session, name) + "'").executeUpdate();
    }

    /**
     * Method to blacklist a {@link Device} to avoid its reconnection or its possibility to use the same
     * {@link Session}
     * 
     * @param device: device to blacklist
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void blacklistDevice(Device device) throws Exception {
        blacklistDevice(device.getSession(), device.getName());
    }

    /**
     * Method to blacklist a {@link Device} to avoid its reconnection or its possibility to use the same
     * {@link Session}
     *
     * @param session: session linked to the device to blacklist
     * @param name:    name of the device
     * @throws Exception when an error occurred
     **/
    public void blacklistDevice(Session session, String name) throws Exception {
        changeDeviceRow(session, name, blacklisted.name(), true);
    }

    /**
     * Method to unblacklist a {@link Device} to allow its reconnection or its possibility to use the same
     * {@link Session}
     * 
     * @param device: device to unblacklist
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void unblacklistDevice(Device device) throws Exception {
        unblacklistDevice(device.getSession(), device.getName());
    }

    /**
     * Method to unblacklist a {@link Device} to allow its reconnection or its possibility to use the same
     * {@link Session}
     *
     * @param session: session linked to the device to blacklist
     * @param name:         name of the device
     *
     * @throws Exception when an error occurred
     **/
    public void unblacklistDevice(Session session, String name) throws Exception {
        changeDeviceRow(session, name, blacklisted.name(), false);
    }

    /**
     * Method to change the permission for a {@link Device} to allow it for a specific operations
     *
     * @param session:    session linked to the device
     * @param device:     the device to change the permission
     * @param permission: the permission to set
     * @throws Exception when an error occurred
     */
    @Wrapper
    public void changeDevicePermission(Session session, Device device, DevicePermission permission) throws Exception {
        changeDevicePermission(session, device.getName(), permission);
    }

    /**
     * Method to change the permission for a {@link Device} to allow it for a specific operations
     *
     * @param session:    session linked to the device
     * @param name:       the device name to change the permission
     * @param permission: the permission to set
     * @throws Exception when an error occurred
     */
    public void changeDevicePermission(Session session, String name, DevicePermission permission) throws Exception {
        changeDeviceRow(session, name, DeviceKeys.permission.name(), permission);
    }

    /**
     * Method to change a {@link Device} detail
     *
     * @param session: session linked to the device
     * @param name:    the device name to change the detail
     * @param column:  the column of the value to set
     * @param value:   the value to set
     * @throws Exception when an error occurred
     */
    private <T> void changeDeviceRow(Session session, String name, String column, T value) throws Exception {
        connection.prepareStatement("UPDATE " + devices + " SET " + column + "='" + encrypt(session, value)
                + "' WHERE token='" + encrypt(session, session.getToken()) + "' AND name='" + encrypt(session, name)
                + "'").executeUpdate();
    }

    /**
     * Method to remove a device from the {@link Table#devices} table
     *
     * @param device: device to delete
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void deleteDevice(Device device) throws Exception {
        deleteDevice(device.getSession(), device.getName());
    }

    /**
     * Method to remove a device from the {@link Table#devices} table
     *
     * @param session: session which the {@link Device} is connected to delete
     * @param name: name of the {@link Device} to delete
     *
     * @throws Exception when an error occurred
     **/
    public void deleteDevice(Session session, String name) throws Exception {
        connection.prepareStatement("DELETE FROM " + devices + " WHERE token='" + encrypt(session, session.getToken())
                + "' AND name='" + encrypt(session, name) + "'").executeUpdate();
    }

    /**
     * Method to insert a new password in the {@link Table#passwords} table
     * @param password: device to insert
     * @apiNote the password will link with a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void insertNewPassword(Password password) throws Exception {
        insertNewPassword(password.getSession(), password.getTail(), new ArrayList<>(password.getScopes()), 
                password.getPassword());
    }

    /**
     * Method to insert a new password in the {@link Table#passwords} table
     *
     * @param session: session value
     * @param tail:     tail of the password
     * @param scopes:   list of scopes where the password can be used
     * @param password: password value
     * @apiNote the password will link with a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws SQLException when an error occurred
     **/
    public void insertNewPassword(Session session, String tail, ArrayList<String> scopes, String password) throws Exception {
        if (scopes == null)
            scopes = new ArrayList<>();
        connection.prepareStatement("INSERT INTO " + passwords + " (token, tail, scopes, password, status) " 
                + "VALUES ('" + encrypt(session, session.getToken()) + "','" + encrypt(session, tail) 
                + "','" + encrypt(session, scopes) + "','" + encrypt(session, password) + "','" 
                + encrypt(session, ACTIVE) + "')").executeUpdate();
    }

    /**
     * Method to add a scope to scopes list for the {@link Password}
     *
     * @param session: session which the {@link Password} is connected
     * @param password: password from edit the scope
     * @param addScope: scope to add
     * @throws Exception when an error occurred
     **/
    public void addPasswordScope(Session session, Password password, String addScope) throws Exception {
        ArrayList<String> currentScopes = new ArrayList<>(password.getScopes());
        if(!currentScopes.contains(addScope)) {
            currentScopes.add(addScope);
            modifyPasswordScopes(session, password.getTail(), currentScopes);
        } else
            throw new SQLException();
    }

    /**
     * Method to edit a scope of scopes list for the {@link Password}
     *
     * @param session: the session which the {@link Password} is connected
     * @param password: password from edit the scope
     * @param oldScope: old scope to edit
     * @param editScope: scope edited
     * @throws Exception when an error occurred
     **/
    public void editPasswordScope(Session session, Password password, String oldScope, String editScope) throws Exception {
        ArrayList<String> currentScopes = new ArrayList<>(password.getScopes());
        if(currentScopes.remove(oldScope) && !currentScopes.contains(editScope)) {
            currentScopes.add(editScope);
            modifyPasswordScopes(session, password.getTail(), currentScopes);
        } else
            throw new SQLException();
    }

    /**
     * Method to remove a scope from scopes list for the {@link Password}
     *
     * @param session: the session which the {@link Password} is connected
     * @param password: password from remove the scope
     * @param removeScope: scope to remove
     * @throws Exception when an error occurred
     **/
    public void removePasswordScope(Session session, Password password, String removeScope) throws Exception {
        ArrayList<String> currentScopes = new ArrayList<>(password.getScopes());
        if(currentScopes.remove(removeScope))
            modifyPasswordScopes(session, password.getTail(), currentScopes);
        else
            throw new SQLException();
    }

    /**
     * Method to set a new scopes list for the {@link Password}
     *
     * @param session: the session which the {@link Password} is connected
     * @param tail: tail of the {@link Password}
     * @param scopes: new list of scopes to set for the {@link Password}
     * @throws Exception when an error occurred
     **/
    private void modifyPasswordScopes(Session session, String tail, ArrayList<String> scopes) throws Exception {
        connection.prepareStatement("UPDATE " + passwords + " SET scopes='" + encrypt(session, scopes) 
                + "' WHERE token='" + encrypt(session, session.getToken()) + "' AND tail='" + encrypt(session, tail) 
                + "'").executeUpdate();
    }

    /**
     * Method to get a {@link Password} from the database
     * @param session: session linked to the password to fetch
     *
     * @return password as {@link Password}
     * @throws Exception when an error occurred
     **/
    public Password getPassword(Session session, String tail) throws Exception {
        ResultSet rPassword = fetchRecord("SELECT * FROM " + passwords + " WHERE token='" + encrypt(session, 
                session.getToken()) + "' AND tail='" + encrypt(session, tail) + "'");
        if(rPassword.next()) {
            Password password = new Password(session,
                    decrypt(session, rPassword.getString(PasswordKeys.tail.name())),
                    fetchScopes(new JSONArray(decrypt(session, rPassword.getString(scopes.name())))),
                    decrypt(session, rPassword.getString(PasswordKeys.password.name())),
                    Status.valueOf(decrypt(session, rPassword.getString(status.name()))));
            rPassword.close();
            return password;
        }
        return null;
    }

    /**
     * Method to get a list of {@link Password} from the database
     * @param session: session linked to the password to fetch
     * @param insertSession: whether insert the session, if {@code "false"} the session will be set as null
     *
     * @return passwords list as {@link ArrayList} of {@link Password}
     * @throws Exception when an error occurred
     **/
    public ArrayList<Password> getPasswords(Session session, boolean insertSession) throws Exception {
        ResultSet rPasswords = fetchRecord("SELECT * FROM " + passwords + " WHERE token='" + encrypt(session, 
                session.getToken()) + "'");
        ArrayList<Password> passwords = new ArrayList<>();
        while (rPasswords.next()) {
            Session iSession = null;
            if(insertSession)
                iSession = session;
            passwords.add(new Password(iSession,
                    decrypt(session, rPasswords.getString(tail.name())),
                    fetchScopes(new JSONArray(decrypt(session, rPasswords.getObject(scopes.name())))),
                    decrypt(session, rPasswords.getString(password.name())),
                    Status.valueOf(decrypt(session, rPasswords.getString(status.name())))
            ));
        }
        rPasswords.close();
        return passwords;
    }

    /**
     * Method to set the password status as {@link Status#DELETED}, so it can be also recoverable
     *
     * @param password: password to set as {@link Status#DELETED}
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void deletePassword(Password password) throws Exception {
        deletePassword(password.getSession(), password.getTail());
    }

    /**
     * Method to set the password status as {@link Status#DELETED}, so it can be also recoverable
     *
     * @param session: session which the {@link Password} is connected to set as {@link Status#DELETED}
     * @param tail: tail of the {@link Password} to set as {@link Status#DELETED}
     * @throws Exception when an error occurred
     **/
    public void deletePassword(Session session, String tail) throws Exception {
        changePasswordStatus(session, tail, DELETED);
    }

    /**
     * Method to set the password status as {@link Status#ACTIVE}, so it can be usable again
     *
     * @param password: password to set as {@link Status#ACTIVE}
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void recoverPassword(Password password) throws Exception {
        deletePassword(password.getSession(), password.getTail());
    }

    /**
     * Method to set the password status as {@link Status#ACTIVE}, so it can be usable again
     *
     * @param session: session which the {@link Password} is connected to set as {@link Status#DELETED}
     * @param tail: tail of the {@link Password} to set as {@link Status#ACTIVE}
     * @throws Exception when an error occurred
     **/
    public void recoverPassword(Session session, String tail) throws Exception {
        changePasswordStatus(session, tail, ACTIVE);
    }

    /**
     * Method to change the password status
     *
     * @param session: session which the {@link Password} is connected
     * @param tail: tail of the {@link Password}
     * @param status: status to set
     * @throws Exception when an error occurred
     **/
    private void changePasswordStatus(Session session, String tail, Status status) throws Exception {
        connection.prepareStatement("UPDATE " + passwords + " SET status='" + encrypt(session, status)
                + "' WHERE token='" + encrypt(session, session.getToken()) + "' AND tail='" + encrypt(session, tail)
                + "'").executeUpdate();
    }

    /**
     * Method to remove permanently a password from the {@link Table#passwords} table
     *
     * @param password: password to delete
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void permanentlyDeletePassword(Password password) throws Exception {
        permanentlyDeletePassword(password.getSession(), password.getTail());
    }

    /**
     * Method to remove permanently a password from the {@link Table#passwords} table
     *
     * @param session: session which the {@link Password} is connected to delete
     * @param tail: tail of the {@link Password} to delete
     * @throws Exception when an error occurred
     **/
    public void permanentlyDeletePassword(Session session, String tail) throws Exception {
        connection.prepareStatement("DELETE FROM " + passwords + " WHERE token='" + encrypt(session,
                session.getToken()) + "' AND tail='" + encrypt(session, tail) + "'").executeUpdate();
    }

    /**
     * Method to get {@link #databasePath} instance <br>
     * No-any params required
     *
     * @return {@link #databasePath} instance as {@link String}
     **/
    public String getDatabasePath() {
        return databasePath;
    }

    /**
     * Method to encrypt a record
     *
     * @param session: session of the value
     * @param value:   value to encrypt
     * @return value encrypted as {@link String}
     * @throws Exception when an error occurred
     **/
    private <T> String encrypt(Session session, T value) throws Exception {
        return doAES(session.getIvSpec(), session.getSecretKey(), true, value);
    }

    /**
     * Method to encrypt a record
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     * @param value: value to encrypt
     *
     * @return value encrypted as {@link String}
     * @throws Exception when an error occurred
     **/
    private <T> String encrypt(String ivSpec, String secretKey, T value) throws Exception {
        return doAES(ivSpec, secretKey, true, value);
    }

    /**
     * Method to decrypt a record
     * @param session: session of the value
     * @param value: value to encrypt
     *
     * @return value decrypted as {@link String}
     * @throws Exception when an error occurred
     **/
    private <T> String decrypt(Session session, T value) throws Exception {
        return doAES(session.getIvSpec(), session.getSecretKey(), false, value);
    }

    /**
     * Method to decrypt a record
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     * @param value: value to encrypt
     *
     * @return value decrypted as {@link String}
     * @throws Exception when an error occurred
     **/
    private <T> String decrypt(String ivSpec, String secretKey, T value) throws Exception {
        return doAES(ivSpec, secretKey, false, value);
    }

    /**
     * Method to execute AES on a record
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     * @param encrypt: whether encrypt or decrypt
     * @param value: value to work with
     *
     * @return value with AES execution as {@link String}
     * @throws Exception when an error occurred
     **/
    private <T> String doAES(String ivSpec, String secretKey, boolean encrypt, T value) throws Exception {
        AESClientCipher cipher = new AESClientCipher(ivSpec, secretKey, CBC_ALGORITHM);
        if(encrypt)
            return cipher.encryptBase64(value.toString());
        else
            return cipher.decryptBase64(value.toString());
    }

    /**
     * Method to fetch a record from the database
     * @param query: query to execute to fetch the record
     *
     * @return query result as {@link ResultSet}
     * @throws SQLException when an error occurred
     **/
    private ResultSet fetchRecord(String query) throws SQLException {
        return connection.createStatement().executeQuery(query);
    }

}
