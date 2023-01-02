package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.annotations.Wrapper;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Session;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;

import static com.tecknobit.glider.helpers.DatabaseManager.Table.*;
import static com.tecknobit.glider.records.Device.Type;
import static com.tecknobit.glider.records.Password.PASSWORD_KEY;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;
import static com.tecknobit.glider.records.Session.*;

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
                    "qr_code_login BOOLEAN NOT NULL);"
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
        insertNewSession(session.getToken(), session.getIvSpec(), session.getSecretKey(), session.getPassword(),
                session.getHostAddress(), session.getHostPort(), session.isSingleUseMode(), session.isQRCodeLoginEnabled());
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
     * @throws SQLException when an error occurred
     **/
    public void insertNewSession(String token, String ivSpec, String secretKey, String password, String hostAddress,
                                 int hostPort, boolean singleUseMode, boolean QRCodeLoginEnabled) throws SQLException {
        connection.prepareStatement("INSERT INTO " + sessions + "(token, iv_spec, secret_key, password, host_address,"
                + " host_port, single_use_mode, qr_code_login)" + " VALUES('"+ token + "','" + ivSpec + "','" + secretKey
                + "','" + password + "','" + hostAddress + "','" + hostPort + "','" + singleUseMode + "','" +
                QRCodeLoginEnabled + "')").executeUpdate();
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
            Session session = new Session(rSession.getString(TOKEN_KEY),
                    rSession.getString(IV_SPEC_KEY),
                    rSession.getString(SECRET_KEY),
                    rSession.getString(PASSWORD_KEY),
                    rSession.getString(HOST_ADDRESS_KEY),
                    rSession.getInt(HOST_PORT_KEY),
                    rSession.getBoolean(SINGLE_USE_MODE_KEY),
                    rSession.getBoolean(QR_CODE_LOGIN_KEY)
            );
            rSession.close();
            return session;
        }
        return null;
    }

    /**
     * Method to insert a new device in the {@link Table#devices} table
     * @param device: device to insert
     * @apiNote the device will with a one {@link Session} inserted in the {@link Table#sessions} table
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
        connection.prepareStatement("INSERT INTO " + passwords + "(token, tail, scopes, password, status) "
                + "VALUES ('" + token + "','" + tail + "','" + scopes + "','" + password + "','" + ACTIVE + "')")
                .executeUpdate();
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
     * Method to remove a password from the {@link Table#passwords} table
     *
     * @param password: password to delete
     * @throws SQLException when an error occurred
     **/
    @Wrapper
    public void deletePassword(Password password) throws SQLException {
        deletePassword(password.getToken(), password.getTail());
    }

    /**
     * Method to remove a password from the {@link Table#passwords} table
     *
     * @param token: token of the session which the {@link Password} is connected to delete
     * @param tail: tail of the {@link Password} to delete
     * @throws SQLException when an error occurred
     **/
    public void deletePassword(String token, String tail) throws SQLException {
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