package com.tecknobit.glider.helpers;

import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Session;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import static com.tecknobit.glider.helpers.DatabaseManager.Table.*;
import static com.tecknobit.glider.records.Device.Type;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;

public class DatabaseManager {

    public enum Table {

        sessions,
        devices,
        passwords

    }

    private final Connection connection;

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

    public void insertNewSession(Session session) throws SQLException {
        insertNewSession(session.getToken(), session.getIvSpec(), session.getSecretKey(), session.getPassword(),
                session.getHostAddress(), session.getHostPort());
    }

    public void insertNewSession(String token, String ivSpec, String secretKey, String password, String host,
                                 int port) throws SQLException {
        connection.prepareStatement("INSERT INTO " + sessions + "(token, iv_spec, secret_key, password, host, port)"
                + " VALUES('"+ token + "','" + ivSpec + "','" + secretKey + "','" + password + "','" + host
                + "','" + port + "')").executeUpdate();
    }

    public void insertNewDevice(Device device) throws SQLException {
        insertNewDevice(device.getToken(), device.getName(), device.getIpAddress(), device.getLoginDateTimestamp(),
                device.getType());
    }

    public void insertNewDevice(String token, String name, String ipAddress, long loginDate, Type type) throws SQLException {
        connection.prepareStatement("INSERT INTO " + devices + "(token, name, ip_address, login_date, last_activity,"
                + "type, blacklisted) VALUES('" + token + "','" + name + "','" + ipAddress + "','" + loginDate + "','"
                + loginDate + "','" + type + "','" + false + "')").executeUpdate();
    }

    public void insertNewPassword(Password password) throws SQLException {
        insertNewPassword(password.getToken(), password.getTail(), new ArrayList<>(password.getScopes()),
                password.getPassword());
    }

    public void insertNewPassword(String token, String tail, ArrayList<String> scopes, String password) throws SQLException {
        connection.prepareStatement("INSERT INTO " + passwords + "(token, tail, scopes, password, status) "
                + "VALUES ('" + token + "','" + tail + "','" + scopes + "','" + password + "','" + ACTIVE + "')")
                .executeUpdate();
    }

    public void deleteSession(Session session) throws SQLException {
        deleteSession(session.getToken());
    }

    public void deleteSession(String token) throws SQLException {
        connection.prepareStatement("DELETE FROM " + sessions + " WHERE token='" + token + "'").executeUpdate();
    }

    public void deleteDevice(Device device) throws SQLException {
        deleteDevice(device.getToken(), device.getName(), device.getIpAddress());
    }

    public void deleteDevice(String token, String name, String ipAddress) throws SQLException {
        connection.prepareStatement("DELETE FROM " + devices + " WHERE token='" + token + "' AND name='"
                + name + "' AND ip_address='" + ipAddress + "'").executeUpdate();
    }

    public void deletePassword(Password password) throws SQLException {
        deletePassword(password.getToken(), password.getTail());
    }

    public void deletePassword(String token, String tail) throws SQLException {
        connection.prepareStatement("DELETE FROM " + passwords + " WHERE token='" + token
                + "' AND tail='" + tail + "'").executeUpdate();
    }

}
