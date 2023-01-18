package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.apis.QRCodeHelper;
import com.tecknobit.apimanager.apis.SocketManager;
import com.tecknobit.apimanager.exceptions.SaveData;
import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Session;
import com.tecknobit.glider.records.Session.SessionKeys;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.File;
import java.io.IOException;
import java.net.BindException;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;
import java.util.UUID;

import static com.tecknobit.apimanager.apis.SocketManager.StandardResponseCode.FAILED;
import static com.tecknobit.apimanager.apis.SocketManager.StandardResponseCode.SUCCESSFUL;
import static com.tecknobit.apimanager.apis.encryption.aes.ClientCipher.Algorithm.CBC_ALGORITHM;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCIvParameterSpecString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCSecretKeyString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.GenericServerCipher.KeySize.k256;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.devices;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.passwords;
import static com.tecknobit.glider.helpers.GliderLauncher.GliderKeys.*;
import static com.tecknobit.glider.helpers.GliderLauncher.Operation.CONNECT;
import static com.tecknobit.glider.helpers.GliderLauncher.Operation.GET_PUBLIC_KEYS;
import static com.tecknobit.glider.records.Device.DeviceKeys.*;
import static com.tecknobit.glider.records.Password.*;
import static com.tecknobit.glider.records.Password.PasswordKeys.*;
import static com.tecknobit.glider.records.Password.PasswordKeys.scope;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;
import static com.tecknobit.glider.records.Password.Status.DELETED;
import static com.tecknobit.glider.records.Password.fetchScopes;
import static com.tecknobit.glider.records.Session.SessionKeys.*;
import static java.lang.System.currentTimeMillis;
import static java.net.InetAddress.getLoopbackAddress;

/**
 * The {@link GliderLauncher} is class useful to start the {@code Glider}'s service <br>
 * You can choose between different configuration to start the service:
 * <ul>
 *     <li>
 *         <b>Login modality</b>
 *         <ul>
 *             <li>
 *                 QR-CODE login enabled -> you can connect from a {@link Device} using the QR-CODE login mode by the
 *                 QR-CODE printed when the service starts or with the normal connection using the correct form to insert
 *                 the connection's data to connect
 *             </li>
 *             <li>
 *                 QR-CODE login disabled -> you can connect from a {@link Device} only  with the normal connection
 *                 using the correct form to insert the connection's data to connect, no-any QR-CODE will be printed
 *             </li>
 *         </ul>
 *     </li>
 *     <li>
 *         <b>Connections modality:</b>
 *         <ul>
 *         <li>
 *              Single use mode -> you can connect only with one {@link Device}, the other attempts by other devices of a
 *              connection will be refused
 *          </li>
 *          <li>
 *              Multiple use mode -> you can connect many {@link Device} as you need and will be linked with the {@link Session}
 *              that is running on your infrastructure
 *           </li>
 *         </ul>
 *     </li>
 * </ul>
 *
 * @author Tecknobit - N7ghtm4r3
 **/
public class GliderLauncher {

    /**
     * {@code Operation} list of available operations
     */
    public enum Operation {

        /**
         * {@code GET_PUBLIC_KEYS} get public keys operation
         */
        GET_PUBLIC_KEYS,

        /**
         * {@code CONNECT} connect operation
         */
        CONNECT,

        /**
         * {@code REFRESH_DATA} refresh data operation
         */
        REFRESH_DATA,

        /**
         * {@code CREATE_PASSWORD} create password operation
         */
        CREATE_PASSWORD,

        /**
         * {@code INSERT_PASSWORD} insert password operation
         */
        INSERT_PASSWORD,

        /**
         * {@code DELETE_PASSWORD} delete password operation
         */
        DELETE_PASSWORD,

        /**
         * {@code RECOVER_PASSWORD} recovery password operation
         */
        RECOVER_PASSWORD,

        /**
         * {@code ADD_SCOPE} add scope operation
         */
        ADD_SCOPE,

        /**
         * {@code EDIT_SCOPE} edit scope operation
         */
        EDIT_SCOPE,

        /**
         * {@code REMOVE_SCOPE} remove scope operation
         */
        REMOVE_SCOPE,

        /**
         * {@code DISCONNECT} disconnect operation
         */
        DISCONNECT,

        /**
         * {@code MANAGE_DEVICE_AUTHORIZATION} blacklist / unblacklist device operation
         */
        MANAGE_DEVICE_AUTHORIZATION,

        /**
         * {@code DELETE_ACCOUNT} account deletion operation
         */
        DELETE_ACCOUNT,

    }

    /**
     * {@code GliderKeys} list of available keys
     */
    public enum GliderKeys {

        /**
         * {@code "ope"} key
         */
        ope,

        /**
         * {@code "statusCode"} key
         */
        statusCode,

        /**
         * {@code "serverStatus"} key
         */
        serverStatus,

        /**
         * {@code "databasePath"} key
         */
        databasePath

    }

    /**
     * {@code COLOR_PRIMARY_HEX} the primary color value as hex {@link String}
     */
    public static final String COLOR_PRIMARY_HEX = "#1E1E8D";

    /**
     * {@code COLOR_RED_HEX} the red color value as hex {@link String}
     */
    public static final String COLOR_RED_HEX = "#A81515";

    /**
     * {@code BACKGROUND_COLOR_HEX} the background color value as hex {@link String}
     */
    public static final String BACKGROUND_COLOR_HEX = "#FAEDE1E1";

    /**
     * {@code databaseManager} manager of the database
     */
    private final DatabaseManager databaseManager;

    /**
     * {@code socketManager} manager of the socket communication
     */
    private final SocketManager socketManager;

    /**
     * {@code session} of the {@code Glider}'s service
     */
    private final Session session;
    
    /**
     * {@code hostPort} host port for {@link #session} of the {@code Glider}'s service
     */
    private final int hostPort;

    /**
     * {@code qrCodeHelper} instance to manage the QRCode login procedure
     */
    private final QRCodeHelper qrCodeHelper;

    /**
     * {@code publicIvSpec} the public instantiation vector
     */
    private String publicIvSpec;

    /**
     * {@code publicCipherKey} the public secret key for the cipher
     */
    private String publicCipherKey;

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param configs: configuration details as <b>JSON<b> {@link File}
     * @implSpec the <b>JSON<b> {@link File} must be formatted as:
     * <pre>
     *     {@code
     *          {
     *              "secretKey": "your_secret_key",
     *              "databasePath": "your_database_path.db",
     *              "ivSpec": "your_iv_spec",
     *              "token": "your_token"
     *          }
     *     }
     * </pre>
     * @apiNote this constructor is used to recreate an old {@link Session} if you need to restart the {@code Glider}'s
     * service
     * @throws Exception when an error occurred
     **/
    public GliderLauncher(File configs) throws Exception {
        this(new JSONObject(new Scanner(configs).useDelimiter("\\Z").next()));
    }

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param configs: configuration details as {@link JSONObject}
     * @implSpec the {@link JSONObject} must be formatted as:
     * <pre>
     *     {@code 
     *          {
     *              "secretKey": "your_secret_key",
     *              "databasePath": "your_database_path.db",
     *              "ivSpec": "your_iv_spec",
     *              "token": "your_token"
     *          }
     *     }
     * </pre>
     * @apiNote this constructor is used to recreate an old {@link Session} if you need to restart the {@code Glider}'s
     * service
     * @throws Exception when an error occurred
     **/
    public GliderLauncher(JSONObject configs) throws Exception {
        this(configs.getString(databasePath.name()), configs.getString(token.name()), configs.getString(ivSpec.name()),
                configs.getString(secretKey.name()));
    }

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param databasePath: path where the database has been created
     * @param token: session token value
     * @param ivSpec             :     {@link IvParameterSpec} of the session
     * @param secretKey          :    {@link SecretKey} of the session
     * @apiNote this constructor is used to recreate an old {@link Session} if you need to restart the {@code Glider}'s
     * service
     **/
    public GliderLauncher(String databasePath, String token, String ivSpec, String secretKey) throws Exception {
        databaseManager = new DatabaseManager(databasePath);
        session = databaseManager.getSession(token, ivSpec, secretKey);
        if(session == null)
            throw new Exception("No-any sessions found with that token, retry");
        socketManager = new SocketManager(false, session.getIvSpec(), session.getSecretKey(), CBC_ALGORITHM);
        this.hostPort = session.getHostPort();
        if(session.isQRCodeLoginEnabled()) {
            qrCodeHelper = new QRCodeHelper();
            createQRCodeCredentials();
        } else
            qrCodeHelper = null;
        refreshPublicKeys();
    }

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param databasePath: path where create the database
     * @param password: password to protect the {@link Session}
     * @param singleUseMode:   whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled:   whether the session allows login by QR-CODE method
     *                          (if enabled will be shown on {@code "hostAddress:(hostPort + 1)"})
     * @param hostPort: host port of the session
     * @param runInLocalhost: whether the session can accept requests outside localhost
     * @apiNote this constructor is used to create a new {@link Session} if you need to start the {@code Glider}'s
     * service for the first time
     * @throws SaveData to safe the {@link Session}'s data
     **/
    public GliderLauncher(String databasePath, String password, boolean singleUseMode, boolean QRCodeLoginEnabled,
                          int hostPort, boolean runInLocalhost) throws Exception {
        if(!databasePath.endsWith(".db"))
            databasePath += ".db";
        databaseManager = new DatabaseManager(databasePath);
        String ivSpec = createCBCIvParameterSpecString();
        String secretKey = createCBCSecretKeyString(k256);
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        databaseManager.insertNewSession(token, ivSpec, secretKey, password, new SocketManager(false)
                        .getHost(!runInLocalhost), hostPort, singleUseMode, QRCodeLoginEnabled, runInLocalhost);
        throw new SaveData("\n" + new JSONObject().put(SessionKeys.ivSpec.name(), ivSpec)
                .put(SessionKeys.secretKey.name(), secretKey)
                .put(SessionKeys.token.name(), token)
                .put(GliderKeys.databasePath.name(), databasePath)
                .toString(4));
    }

    /**
     * Method to start the {@code Glider}'s service with the details 
     * fetched from the database if already exist or with that inserted at the 
     * first run <br>
     * Any params required
     * @throws IOException when an error occurred during {@link #socketManager}'s workflow
     **/
    public void startService() throws IOException {
        JSONObject response = new JSONObject();
        socketManager.setDefaultErrorResponse(new JSONObject().put(statusCode.name(), FAILED));
        socketManager.startListener(hostPort, () -> {
            JSONObject request;
            Device device;
            while (socketManager.continueListening()) {
                System.out.println("Waiting...");
                try {
                    String ipAddress = SocketManager.getIpAddress(socketManager.acceptRequest());
                    try {
                        request = new JSONObject(socketManager.readContent());
                    } catch (IllegalArgumentException | BadPaddingException e) {
                        String privateSecretKey = session.getSecretKey();
                        if(socketManager.getCipherKey().equals(privateSecretKey)) {
                            socketManager.changeCipherKeys(publicIvSpec, publicCipherKey);
                        } else
                            socketManager.changeCipherKeys(session.getIvSpec(), privateSecretKey);
                        try {
                            request = new JSONObject(socketManager.readLastContent());
                        } catch (IllegalArgumentException eP) {
                            socketManager.writePlainContent(new JSONObject().put(ivSpec.name(), publicIvSpec)
                                    .put(secretKey.name(), publicCipherKey));
                            request = null;
                        }
                    }
                    if(request != null) {
                        boolean check = true;
                        if(session.runInLocalhost())
                            check = getLoopbackAddress().getHostAddress().endsWith(ipAddress);
                        if(check && session.getSessionPassword().equals(JsonHelper.getString(request,
                                sessionPassword.name()))) {
                            String deviceName = request.getString(name.name());
                            Operation vOpe = Operation.valueOf(request.getString(ope.name()));
                            System.out.println(vOpe);
                            device = databaseManager.getDevice(session, deviceName, ipAddress);
                            if(vOpe.equals(GET_PUBLIC_KEYS)) {
                                socketManager.writePlainContent(new JSONObject().put(ivSpec.name(), publicIvSpec)
                                        .put(secretKey.name(), publicCipherKey));
                            } else if(vOpe.equals(CONNECT)) {
                                boolean connect = false;
                                if(session.isSingleUseMode()) {
                                    if(databaseManager.getDevices(session, false).size() < 1)
                                        connect = true;
                                    else
                                        socketManager.sendDefaultErrorResponse();
                                } else
                                    connect = true;
                                if((connect && (device == null || !device.isBlacklisted()))) {
                                    // TODO: 03/01/2023 payload:
                                    //  device name
                                    //  type
                                    //  sPassword
                                    databaseManager.insertNewDevice(session, deviceName, ipAddress,
                                            currentTimeMillis(), Device.Type.valueOf(request.getString(type.name())));
                                    // TODO: 03/01/2023 response
                                    // status code
                                    //  session - less the password
                                    //  passwords
                                    //  devices
                                    sendAllData(response, true);
                                    socketManager.changeCipherKeys(session.getIvSpec(), session.getSecretKey());
                                } else
                                    socketManager.sendDefaultErrorResponse();
                            } else {
                                if(!device.isBlacklisted()) {
                                    databaseManager.updateLastActivity(device);
                                    switch (vOpe) {
                                        case REFRESH_DATA -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            sendAllData(response, false);
                                        }
                                        case CREATE_PASSWORD -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            //  scopes
                                            //  length
                                            int length = request.getInt(Password.PasswordKeys.length.name());
                                            if(length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH) {
                                                ArrayList<Integer> letters = new ArrayList<>();
                                                StringBuilder password = new StringBuilder();
                                                for (int j = 0; j < length; j++) {
                                                    int index;
                                                    do {
                                                        index = new Random().nextInt(122);
                                                        if(index < 33)
                                                            index = 33;
                                                    } while (letters.contains(index) || index == 44);
                                                    letters.add(index);
                                                    password.append(((char) index));
                                                }
                                                databaseManager.insertNewPassword(session, request.getString(tail.name()),
                                                        fetchScopes(request.getJSONArray(scopes.name())), password.toString());
                                                // TODO: 03/01/2023 response
                                                // status code
                                                //  password
                                                sendSuccessfulResponse(response.put(PasswordKeys.password.name(), password));
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case INSERT_PASSWORD -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            //  scopes
                                            //  password
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            int length = tail.length();
                                            if(length > 0 && length < 30) {
                                                String password  = request.getString(PasswordKeys.password.name());
                                                length = password.length();
                                                if(length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH) {
                                                    databaseManager.insertNewPassword(session, tail,
                                                            fetchScopes(request.getJSONArray(scopes.name())), password);
                                                    // TODO: 03/01/2023 response
                                                    // status code
                                                    sendSuccessfulResponse(response);
                                                } else
                                                    socketManager.sendDefaultErrorResponse();
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case DELETE_PASSWORD -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if(password != null) {
                                                if(password.getStatus().equals(ACTIVE))
                                                    databaseManager.deletePassword(session, tail);
                                                else
                                                    databaseManager.permanentlyDeletePassword(session, tail);
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case RECOVER_PASSWORD -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if(password != null && password.getStatus().equals(DELETED)) {
                                                databaseManager.recoverPassword(session, tail);
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case ADD_SCOPE -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            // add_scope
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if(password != null) {
                                                databaseManager.addPasswordScope(session, password,
                                                        request.getString(scope.name()));
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case EDIT_SCOPE -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            // edit_scope
                                            // oldScope
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if(password != null) {
                                                databaseManager.editPasswordScope(session, password,
                                                        request.getString(oldScope.name()), request.getString(scope.name()));
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case REMOVE_SCOPE -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  sPassword
                                            //  tail
                                            // remove_scope
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if(password != null) {
                                                databaseManager.removePasswordScope(session, password,
                                                        request.getString(scope.name()));
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case DISCONNECT -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  type
                                            // targetDevice:
                                            //  - device name
                                            //  - ip
                                            if(JsonHelper.getJSONObject(request, targetDevice.name()) != null) {
                                                request = request.getJSONObject(targetDevice.name());
                                                device = databaseManager.getDevice(session, request.getString(name.name()),
                                                        ipAddress);
                                            }
                                            if(device != null) {
                                                databaseManager.deleteDevice(session, device.getName(), device.getIpAddress());
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case MANAGE_DEVICE_AUTHORIZATION -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  type
                                            // targetDevice:
                                            //  - device name
                                            //  - ip
                                            request = request.getJSONObject(targetDevice.name());
                                            device = databaseManager.getDevice(session, request.getString(name.name()),
                                                    ipAddress);
                                            if(device != null) {
                                                if(device.isBlacklisted()) {
                                                    databaseManager.unblacklistDevice(session, device.getName(),
                                                            device.getIpAddress());
                                                } else {
                                                    databaseManager.blacklistDevice(session, device.getName(),
                                                            device.getIpAddress());
                                                }
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case DELETE_ACCOUNT -> {
                                            // TODO: 03/01/2023 payload:
                                            //  device name
                                            //  type
                                            databaseManager.deleteSession(session);
                                            // status code
                                            sendSuccessfulResponse(response);
                                            socketManager.stopListener();
                                            qrCodeHelper.stopHosting();
                                        }
                                        default -> socketManager.sendDefaultErrorResponse();
                                    }
                                } else
                                    socketManager.sendDefaultErrorResponse();
                            }
                        } else
                            socketManager.sendDefaultErrorResponse();
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // TODO: 05/01/2023 TO REMOVE PRINTSTACKTRACE 
                    try {
                        socketManager.sendDefaultErrorResponse();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } finally {
                    response.clear();
                }
            }
            System.out.println("This session has been deleted");
        });
    }

    /**
     * Method to refresh the public keys to cipher the communication <br>
     * Any params required
     **/
    private void refreshPublicKeys() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                while (true) {
                    try {
                        if(session.isSingleUseMode() && databaseManager.getDevices(session, false).size() == 1)
                            break;
                        else {
                            publicIvSpec = createCBCIvParameterSpecString();
                            publicCipherKey = createCBCSecretKeyString(k256);
                            if(session.isQRCodeLoginEnabled()) {
                                qrCodeHelper.stopHosting();
                                createQRCodeCredentials();
                            }
                        }
                        sleep(5000);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }.start();
    }

    /**
     * Method to create and host a QRCode with connection credentials <br>
     * Any params required
     * @throws IOException when an error occurred
     **/
    private void createQRCodeCredentials() throws IOException {
        try {
            qrCodeHelper.hostQRCode(session.getHostPort() + 1, new JSONObject()
                            .put(hostAddress.name(), session.getHostAddress())
                            .put(SessionKeys.hostPort.name(), hostPort)
                            .put(ivSpec.name(), publicIvSpec)
                            .put(secretKey.name(), publicCipherKey)
                            .put(SessionKeys.token.name(), "Glider"), "Glider.png", 250,
                            true, new File("src/main/resources/qrcode.html"));
        } catch (BindException e) {
            System.err.println("You cannot have multiple sessions on the same port at the same time");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Method to send all data of a {@link Session}
     *
     * @param response      : response where link {@link Session}
     * @param insertSession : whether insert the {@link #session} details
     **/
    private void sendAllData(JSONObject response, boolean insertSession) throws Exception {
        if(insertSession) {
            JSONObject jSession = session.toJSON();
            jSession.remove(sessionPassword.name());
            response.put(SessionKeys.session.name(), jSession);
        }
        sendSuccessfulResponse(response.put(passwords.name(), databaseManager.getPasswords(session, false))
                .put(devices.name(), databaseManager.getDevices(session, false)));
    }

    /**
     * Method to send a successful response to the client
     * @param message: message to send
     *
     * @throws Exception when an error occurred
     **/
    private void sendSuccessfulResponse(JSONObject message) throws Exception {
        socketManager.writeContent(message.put(statusCode.name(), SUCCESSFUL));
    }

    /**
     * Method to get {@link #databaseManager} instance <br>
     * Any params required
     *
     * @return {@link #databaseManager} instance as {@link DatabaseManager}
     **/
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Method to get {@link #session} instance <br>
     * Any params required
     *
     * @return {@link #session} instance as {@link Session}
     **/
    public Session getSession() {
        return session;
    }

}
