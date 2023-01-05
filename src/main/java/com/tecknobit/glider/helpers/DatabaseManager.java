package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.annotations.Wrapper;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Device.DeviceKeys;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Password.PasswordKeys;
import com.tecknobit.glider.records.Session;
import org.json.JSONArray;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import static com.tecknobit.apimanager.formatters.TimeFormatter.getStringDate;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.*;
import static com.tecknobit.glider.records.Device.DeviceKeys.*;
import static com.tecknobit.glider.records.Device.Type;
import static com.tecknobit.glider.records.Password.PasswordKeys.*;
import static com.tecknobit.glider.records.Password.Status;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;
import static com.tecknobit.glider.records.Password.Status.DELETED;
import static com.tecknobit.glider.records.Password.fetchScopes;
import static com.tecknobit.glider.records.Session.SessionKeys;
import static com.tecknobit.glider.records.Session.SessionKeys.*;
import static java.lang.Boolean.parseBoolean;

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
     * Constructor to init {@link DatabaseManager} object
     *
     * @param databasePath: path where create the database, if it exists will be only read and not recreated
     * @apiNote will be set the {@link #connection} instance and created the tables if they don't exist
     **/
    public DatabaseManager(String databasePath) throws IOException, SQLException {
        if(!databasePath.endsWith(".db"))
            databasePath += ".db";
        File database = new File(databasePath);
        boolean dbCreated = true;
        if(!database.exists())
            dbCreated = database.createNewFile();
        if(dbCreated) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            Statement statement = connection.createStatement();
            statement.execute("PRAGMA foreign_keys = ON");
            statement.execute("CREATE TABLE IF NOT EXISTS " + sessions + " (\n" +
                    "token VARCHAR(32) PRIMARY KEY, \n" +
                    "iv_spec VARCHAR(24) UNIQUE NOT NULL, \n" +
                    "secret_key VARCHAR(44) UNIQUE NOT NULL, \n" +
                    "password VARCHAR(32) UNIQUE NOT NULL, \n" +
                    "host_address TEXT NOT NULL, \n" +
                    "host_port INTEGER UNIQUE NOT NULL, \n" +
                    "single_use_mode BOOLEAN NOT NULL, \n" +
                    "qr_code_login BOOLEAN NOT NULL, \n" +
                    "run_in_localhost BOOLEAN NOT NULL);"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS " + devices + "(\n" +
                    "token VARCHAR(32) NOT NULL, \n" +
                    "name VARCHAR(50) NOT NULL, \n" +
                    "ip_address VARCHAR(15) NOT NULL, \n" +
                    "login_date INTEGER NOT NULL, \n" +
                    "last_activity INTEGER NOT NULL, \n" +
                    "type VARCHAR(7) NOT NULL, \n" +
                    "blacklisted BOOLEAN DEFAULT FALSE, \n" +
                    "PRIMARY KEY(token, name, ip_address), \n" +
                    "FOREIGN KEY(token) REFERENCES " + sessions + "(token) ON DELETE CASCADE);"
            );
            statement.execute("CREATE TABLE IF NOT EXISTS " + passwords + "(\n" +
                    "token VARCHAR(32) NOT NULL, \n" +
                    "tail VARCHAR(30) NOT NULL, \n" +
                    "scopes TEXT NOT NULL, \n" +
                    "password VARCHAR(32) UNIQUE NOT NULL, \n" +
                    "status VARCHAR(7) NOT NULL, \n" +
                    "PRIMARY KEY(token, tail), \n" +
                    "FOREIGN KEY(token) REFERENCES " + sessions + "(token) ON DELETE CASCADE);"
            );
        } else
            throw new IOException("Cannot create a database file, check the inserted path");
    }

    /**
     * Method to insert a new session in the {@link Table#sessions} table
     * @param session: session to insert
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void insertNewSession(Session session) throws SQLException {
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
     * @throws SQLException when an error occurred
     **/
    public void insertNewSession(String token, String ivSpec, String secretKey, String password, String hostAddress,
                                 int hostPort, boolean singleUseMode, boolean QRCodeLoginEnabled,
                                 boolean runInLocalhost) throws SQLException {
        connection.prepareStatement("INSERT INTO " + sessions + "(token, iv_spec, secret_key, password, host_address,"
                + " host_port, single_use_mode, qr_code_login, run_in_localhost)" + " VALUES('"+ token + "','" + ivSpec
                + "','" + secretKey + "','" + password + "','" + hostAddress + "','" + hostPort + "','" + singleUseMode +
                "','" + QRCodeLoginEnabled + "','" + runInLocalhost + "')").executeUpdate();
    }

    /**
     * Method to get a {@link Session} from the database
     * @param token: token of the session to fetch
     *
     * @return session as {@link Session}
     * @throws SQLException when an error occurred
     **/
    public Session getSession(String token) throws SQLException {
        ResultSet rSession = fetchRecord("SELECT * FROM " + sessions + " WHERE token='" + token + "'");
        if(rSession.next()) {
            Session session = new Session(token,
                    rSession.getString(iv_spec.name()),
                    rSession.getString(secret_key.name()),
                    rSession.getString(password.name()),
                    rSession.getString(host_address.name()),
                    rSession.getInt(host_port.name()),
                    parseBoolean(rSession.getString(single_use_mode.name())),
                    parseBoolean(rSession.getString(qr_code_login.name())),
                    parseBoolean(rSession.getString(run_in_localhost.name())));
            rSession.close();
            return session;
        }
        return null;
    }

    /**
     * Method to remove a session from the {@link Table#sessions} table
     *
     * @param session: session to delete
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void deleteSession(Session session) throws SQLException {
        deleteSession(session.getToken());
    }

    /**
     * Method to remove a session from the {@link Table#sessions} table
     *
     * @param token: token of the session to delete
     * @throws SQLException when an error occurred
     **/
    public void deleteSession(String token) throws SQLException {
        connection.prepareStatement("DELETE FROM " + sessions + " WHERE token='" + token + "'").executeUpdate();
    }

    /**
     * Method to insert a new device in the {@link Table#devices} table
     * @param device: device to insert
     * @apiNote the device will link a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void insertNewDevice(Device device) throws SQLException {
        insertNewDevice(device.getToken(), device.getName(), device.getIpAddress(), device.getLoginDateTimestamp(),
                device.getType());
    }

    /**
     * Method to insert a new device in the {@link Table#devices} table
     *
     * @param token: session token value
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @param loginDate:    loginDate date of the device
     * @param type:         type of the devices
     * @apiNote the device will link with a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws SQLException when an error occurred
     **/
    public void insertNewDevice(String token, String name, String ipAddress, long loginDate, Type type) throws SQLException {
        connection.prepareStatement("INSERT INTO " + devices + "(token, name, ip_address, login_date, last_activity,"
                + "type, blacklisted) VALUES('" + token + "','" + name + "','" + ipAddress + "','" + loginDate + "','"
                + loginDate + "','" + type + "','" + false + "')").executeUpdate();
    }

    /**
     * Method to get a {@link Device} from the database
     * @param token: token of the session linked to the device to fetch
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     *
     * @return device as {@link Device}
     * @throws SQLException when an error occurred
     **/
    public Device getDevice(String token, String name, String ipAddress) throws SQLException {
        ResultSet rDevice = fetchRecord("SELECT * FROM " + devices + " WHERE token='" + token + "' AND name='"
                + name + "' AND ip_address='" + ipAddress + "'");
        if(rDevice.next()) {
            Device device = new Device(token,
                    rDevice.getString(DeviceKeys.name.name()),
                    rDevice.getString(ip_address.name()),
                    getStringDate(rDevice.getLong(login_date.name())),
                    getStringDate(rDevice.getLong(last_activity.name())),
                    Type.valueOf(rDevice.getString(type.name())),
                    parseBoolean(rDevice.getString(blacklisted.name())));
            rDevice.close();
            return device;
        }
        return null;
    }

    /**
     * Method to get a list of {@link Device} from the database
     * @param token: token of the session linked to the devices to fetch
     * @param insertSessionToken: whether insert the session token, if {@code "false"} the session token will be set as null
     *
     * @return devices list as {@link ArrayList} of {@link Device}
     * @throws SQLException when an error occurred
     **/
    public ArrayList<Device> getDevices(String token, boolean insertSessionToken) throws SQLException {
        ResultSet rDevices = fetchRecord("SELECT * FROM " + devices + " WHERE token='" + token + "'");
        ArrayList<Device> devices = new ArrayList<>();
        while (rDevices.next()) {
            String sToken = null;
            if(insertSessionToken)
                sToken = rDevices.getString(SessionKeys.token.name());
            devices.add(new Device(sToken,
                    rDevices.getString(DeviceKeys.name.name()),
                    rDevices.getString(ip_address.name()),
                    getStringDate(rDevices.getLong(login_date.name())),
                    getStringDate(rDevices.getLong(last_activity.name())),
                    Type.valueOf(rDevices.getString(type.name())),
                    parseBoolean(rDevices.getString(blacklisted.name()))
            ));
        }
        rDevices.close();
        return devices;
    }

    /**
     * Method to blacklist a {@link Device} to avoid its reconnection or its possibility to use the same
     * {@link Session}
     *
     * @param device: device to blacklist
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void blacklistDevice(Device device) throws SQLException {
        blacklistDevice(device.getToken(), device.getName(), device.getIpAddress());
    }

    /**
     * Method to blacklist a {@link Device} to avoid its reconnection or its possibility to use the same
     * {@link Session}
     *
     * @param token: token of the session linked to the device to blacklist
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @throws SQLException when an error occurred
     **/
    public void blacklistDevice(String token, String name, String ipAddress) throws SQLException {
        changeDeviceAuthorization(token, name, ipAddress, true);
    }

    /**
     * Method to unblacklist a {@link Device} to allow its reconnection or its possibility to use the same
     * {@link Session}
     *
     * @param device: device to unblacklist
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void unblacklistDevice(Device device) throws SQLException {
        unblacklistDevice(device.getToken(), device.getName(), device.getIpAddress());
    }

    /**
     * Method to unblacklist a {@link Device} to allow its reconnection or its possibility to use the same
     * {@link Session}
     *
     * @param token: token of the session linked to the device to unblacklist
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @throws SQLException when an error occurred
     **/
    public void unblacklistDevice(String token, String name, String ipAddress) throws SQLException {
        changeDeviceAuthorization(token, name, ipAddress, false);
    }

    /**
     * Method to blacklist or unblacklist a {@link Device}
     *
     * @param token: token of the session which the {@link Device} is connected
     * @param name:         name of the device
     * @param ipAddress:    ip address of the device
     * @param blacklist: whether the device have to be blacklisted
     * @throws SQLException when an error occurred
     **/
    private void changeDeviceAuthorization(String token, String name, String ipAddress,
                                           boolean blacklist) throws SQLException {
        connection.prepareStatement("UPDATE " + devices + " SET blacklisted='" + blacklist + "' WHERE token='"
                + token + "' AND name='" + name + "' AND ip_address='" + ipAddress + "'").executeUpdate();
    }

    /**
     * Method to remove a device from the {@link Table#devices} table
     *
     * @param device: device to delete
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void deleteDevice(Device device) throws SQLException {
        deleteDevice(device.getToken(), device.getName(), device.getIpAddress());
    }

    /**
     * Method to remove a device from the {@link Table#devices} table
     *
     * @param token: token of the session which the {@link Device} is connected to delete
     * @param name: name of the {@link Device} to delete
     * @param ipAddress: ip address of the {@link Device} to delete
     * @throws SQLException when an error occurred
     **/
    public void deleteDevice(String token, String name, String ipAddress) throws SQLException {
        connection.prepareStatement("DELETE FROM " + devices + " WHERE token='" + token + "' AND name='"
                + name + "' AND ip_address='" + ipAddress + "'").executeUpdate();
    }

    /**
     * Method to insert a new password in the {@link Table#passwords} table
     * @param password: device to insert
     * @apiNote the password will link with a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void insertNewPassword(Password password) throws SQLException {
        insertNewPassword(password.getToken(), password.getTail(), new ArrayList<>(password.getScopes()),
                password.getPassword());
    }

    /**
     * Method to insert a new password in the {@link Table#passwords} table
     *
     * @param token: session token value
     * @param tail:     tail of the password
     * @param scopes:   list of scopes where the password can be used
     * @param password: password value
     * @apiNote the password will link with a one {@link Session} inserted in the {@link Table#sessions} table
     * @throws SQLException when an error occurred
     **/
    public void insertNewPassword(String token, String tail, ArrayList<String> scopes, String password) throws SQLException {
        if (scopes == null)
            scopes = new ArrayList<>();
        connection.prepareStatement("INSERT INTO " + passwords + " (token, tail, scopes, password, status) "
                + "VALUES ('" + token + "','" + tail + "','" + scopes + "','" + password + "','" + ACTIVE + "')")
                .executeUpdate();
    }

    /**
     * Method to add a scope to scopes list for the {@link Password}
     *
     * @param token: token of the session which the {@link Password} is connected
     * @param password: password from edit the scope
     * @param addScope: scope to add
     * @throws SQLException when an error occurred
     **/
    public void addPasswordScope(String token, Password password, String addScope) throws SQLException {
        ArrayList<String> currentScopes = new ArrayList<>(password.getScopes());
        if(!currentScopes.contains(addScope)) {
            currentScopes.add(addScope);
            modifyPasswordScopes(token, password.getTail(), currentScopes);
        } else
            throw new SQLException();
    }

    /**
     * Method to edit a scope of scopes list for the {@link Password}
     *
     * @param token: token of the session which the {@link Password} is connected
     * @param password: password from edit the scope
     * @param oldScope: old scope to edit
     * @param editScope: scope edited
     * @throws SQLException when an error occurred
     **/
    public void editPasswordScope(String token, Password password, String oldScope, String editScope) throws SQLException {
        ArrayList<String> currentScopes = new ArrayList<>(password.getScopes());
        if(currentScopes.remove(oldScope) && !currentScopes.contains(editScope)) {
            currentScopes.add(editScope);
            modifyPasswordScopes(token, password.getTail(), currentScopes);
        } else
            throw new SQLException();
    }

    /**
     * Method to remove a scope from scopes list for the {@link Password}
     *
     * @param token: token of the session which the {@link Password} is connected
     * @param password: password from remove the scope
     * @param removeScope: scope to remove
     * @throws SQLException when an error occurred
     **/
    public void removePasswordScope(String token, Password password, String removeScope) throws SQLException {
        ArrayList<String> currentScopes = new ArrayList<>(password.getScopes());
        if(currentScopes.remove(removeScope))
            modifyPasswordScopes(token, password.getTail(), currentScopes);
        else
            throw new SQLException();
    }

    /**
     * Method to set a new scopes list for the {@link Password}
     *
     * @param token: token of the session which the {@link Password} is connected
     * @param tail: tail of the {@link Password}
     * @param scopes: new list of scopes to set for the {@link Password}
     * @throws SQLException when an error occurred
     **/
    private void modifyPasswordScopes(String token, String tail, ArrayList<String> scopes) throws SQLException {
        connection.prepareStatement("UPDATE " + passwords + " SET scopes='" + scopes + "' WHERE token='"
                + token + "' AND tail='" + tail + "'").executeUpdate();
    }

    /**
     * Method to get a {@link Password} from the database
     * @param token: token of the session linked to the password to fetch
     *
     * @return password as {@link Password}
     * @throws SQLException when an error occurred
     **/
    public Password getPassword(String token, String tail) throws SQLException {
        ResultSet rPassword = fetchRecord("SELECT * FROM " + passwords + " WHERE token='" + token + "' AND tail='"
                + tail + "'");
        if(rPassword.next()) {
            Password password = new Password(token,
                    rPassword.getString(PasswordKeys.tail.name()),
                    fetchScopes(new JSONArray(rPassword.getString(scopes.name()))),
                    rPassword.getString(PasswordKeys.password.name()),
                    Status.valueOf(rPassword.getString(status.name())));
            rPassword.close();
            return password;
        }
        return null;
    }

    /**
     * Method to get a list of {@link Password} from the database
     * @param token: token of the session linked to the passwords to fetch
     * @param insertSessionToken: whether insert the session token, if {@code "false"} the session token will be set as null
     *
     * @return passwords list as {@link ArrayList} of {@link Password}
     * @throws SQLException when an error occurred
     **/
    public ArrayList<Password> getPasswords(String token, boolean insertSessionToken) throws SQLException {
        ResultSet rPasswords = fetchRecord("SELECT * FROM " + passwords + " WHERE token='" + token + "'");
        ArrayList<Password> passwords = new ArrayList<>();
        while (rPasswords.next()) {
            String sToken = null;
            if(insertSessionToken)
                sToken = rPasswords.getString(SessionKeys.token.name());
            passwords.add(new Password(sToken,
                    rPasswords.getString(tail.name()),
                    fetchScopes(new JSONArray(rPasswords.getString(scopes.name()))),
                    rPasswords.getString(password.name()),
                    Status.valueOf(rPasswords.getString(status.name()))
            ));
        }
        rPasswords.close();
        return passwords;
    }

    /**
     * Method to set the password status as {@link Status#DELETED}, so it can be also recoverable
     *
     * @param password: password to set as {@link Status#DELETED}
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void deletePassword(Password password) throws SQLException {
        deletePassword(password.getToken(), password.getTail());
    }

    /**
     * Method to set the password status as {@link Status#DELETED}, so it can be also recoverable
     *
     * @param token: token of the session which the {@link Password} is connected to set as {@link Status#DELETED}
     * @param tail: tail of the {@link Password} to set as {@link Status#DELETED}
     * @throws SQLException when an error occurred
     **/
    public void deletePassword(String token, String tail) throws SQLException {
        changePasswordStatus(token, tail, DELETED);
    }

    /**
     * Method to set the password status as {@link Status#ACTIVE}, so it can be usable again
     *
     * @param password: password to set as {@link Status#ACTIVE}
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void recoverPassword(Password password) throws SQLException {
        deletePassword(password.getToken(), password.getTail());
    }

    /**
     * Method to set the password status as {@link Status#ACTIVE}, so it can be usable again
     *
     * @param token: token of the session which the {@link Password} is connected to set as {@link Status#DELETED}
     * @param tail: tail of the {@link Password} to set as {@link Status#ACTIVE}
     * @throws SQLException when an error occurred
     **/
    public void recoverPassword(String token, String tail) throws SQLException {
        changePasswordStatus(token, tail, ACTIVE);
    }

    /**
     * Method to change the password status
     *
     * @param token: token of the session which the {@link Password} is connected
     * @param tail: tail of the {@link Password}
     * @param status: status to set
     * @throws SQLException when an error occurred
     **/
    private void changePasswordStatus(String token, String tail, Status status) throws SQLException {
        connection.prepareStatement("UPDATE " + passwords + " SET status='" + status + "' WHERE token='" + token
                + "' AND tail='" + tail + "'").executeUpdate();
    }

    /**
     * Method to remove permanently a password from the {@link Table#passwords} table
     *
     * @param password: password to delete
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void permanentlyDeletePassword(Password password) throws SQLException {
        permanentlyDeletePassword(password.getToken(), password.getTail());
    }

    /**
     * Method to remove permanently a password from the {@link Table#passwords} table
     *
     * @param token: token of the session which the {@link Password} is connected to delete
     * @param tail: tail of the {@link Password} to delete
     * @throws SQLException when an error occurred
     **/
    public void permanentlyDeletePassword(String token, String tail) throws SQLException {
        connection.prepareStatement("DELETE FROM " + passwords + " WHERE token='" + token
                + "' AND tail='" + tail + "'").executeUpdate();
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
