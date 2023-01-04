package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.annotations.Wrapper;
import com.tecknobit.apimanager.apis.SocketManager;
import com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher;
import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Session;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCIvParameterSpecString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCSecretKeyString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.GenericServerCipher.KeySize.k256;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.devices;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.passwords;
import static com.tecknobit.glider.helpers.GliderLauncher.GliderKeys.ope;
import static com.tecknobit.glider.helpers.GliderLauncher.GliderKeys.status_code;
import static com.tecknobit.glider.helpers.GliderLauncher.Operation.CONNECT;
import static com.tecknobit.glider.records.Device.DeviceKeys.*;
import static com.tecknobit.glider.records.Password.*;
import static com.tecknobit.glider.records.Password.PasswordKeys.*;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;
import static com.tecknobit.glider.records.Password.Status.DELETED;
import static com.tecknobit.glider.records.Session.SessionKeys.session_password;
import static java.lang.System.currentTimeMillis;

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
// TODO: 04/01/2023 PASS A CONFIG JSON TO INIT SERVER DIRECTLY FROM A JAR FILE, THINK ABOUT IT
public class GliderLauncher {

    /**
     * {@code Operation} list of available operations
     */
    public enum Operation {

        /**
         * {@code CONNECT} connect operation
         */
        CONNECT,

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
         * {@code "status_code"} key
         */
        status_code,

        /**
         * {@code "server_status"} key
         */
        server_status

    }

    /**
     * {@code ERROR_STATUS} error status code
     */
    public static final int ERROR_STATUS = 500;

    /**
     * {@code SUCCESSFUL_STATUS} successful status code
     */
    public static final int SUCCESSFUL_STATUS = 200;

    /**
     * {@code GENERIC_STATUS} generic status code
     */
    public static final int GENERIC_STATUS = 300;

    /**
     * {@code COLOR_PRIMARY_HEX} the primary color value as hex {@link String}
     */
    public static final String COLOR_PRIMARY_HEX = "#1E1E8D";

    /**
     * {@code COLOR_RED_HEX} the red color value as hex {@link String}
     */
    public static final String COLOR_RED_HEX = "#A81515";

    /**
     * {@code databaseManager} manager of the database
     */
    private final DatabaseManager databaseManager;

    /**
     * {@code serverCipher} useful to encrypt all the {@link Session}'s data 
     */
    private final CBCServerCipher serverCipher;

    /**
     * {@code socketManager} manager of the socket communication
     */
    private final SocketManager socketManager;

    /**
     * {@code session} of the {@code Glider}'s service
     */
    private Session session;
    
    /**
     * {@code hostPort} host port for {@link #session} of the {@code Glider}'s service
     */
    private final int hostPort;

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param databasePath: path where the database has been created
     * @param token: session token value
     * @apiNote this constructor is to use to recreate an old {@link Session} if you need to restart the {@code Glider}'s
     * service
     **/
    public GliderLauncher(String databasePath, String token) throws Exception {
        databaseManager = new DatabaseManager(databasePath);
        session = databaseManager.getSession(token);
        if(session == null)
            throw new Exception("No-any sessions found with that token, retry");
        socketManager = new SocketManager(false);
        serverCipher = new CBCServerCipher(session.getIvSpec(), session.getSecretKey(), k256);
        this.hostPort = session.getHostPort();
    }

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param databasePath: path where create the database
     * @param password: password to protect the {@link Session}
     * @param singleUseMode:   whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled:   whether the session allows login by QR-CODE method
     * @param hostPort: host port of the session
     * @apiNote this constructor is to use to create a new {@link Session} if you need to start the {@code Glider}'s
     * service for the first time
     **/
    public GliderLauncher(String databasePath, String password, boolean singleUseMode, boolean QRCodeLoginEnabled,
                          int hostPort) throws Exception {
        databaseManager = new DatabaseManager(databasePath);
        socketManager = new SocketManager(false);
        session = new Session(createToken(), createCBCIvParameterSpecString(), createCBCSecretKeyString(k256), password,
                "localhost", hostPort, singleUseMode, QRCodeLoginEnabled); // TODO: 02/01/2023 USE socketManager.getHost() INSTEAD "localhost"
        databaseManager.insertNewSession(session);
        serverCipher = new CBCServerCipher(session.getIvSpec(), session.getSecretKey(), k256);
        this.hostPort = hostPort;
        // TODO: 03/01/2023 QR-CODE LOGIN IF ENABLED
    }

    /**
     * Method to create a new {@link Session}'s token <br>
     * Any params required
     *
     * @return new {@link Session}'s token as {@link String}
     **/
    private String createToken() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * Method to start the {@code Glider}'s service with the details 
     * fetched from the database if already exist or with that inserted at the 
     * first run <br>
     * Any params required
     * @throws IOException when an error occurred during {@link #socketManager}'s workflow
     **/
    public void startService() throws IOException {
        String sessionToken = session.getToken();
        JSONObject response = new JSONObject();
        socketManager.startListener(hostPort, () -> {
            Device device;
            while (session != null) {
                System.out.println("Waiting...");
                try {
                    Socket sRequest = socketManager.acceptRequest();
                    JSONObject request = new JSONObject(serverCipher.decryptRequest(socketManager.readContent()));
                    System.out.println(request);
                    request.put(ip_address.name(), ((InetSocketAddress)sRequest.getRemoteSocketAddress()).getAddress()
                            .getHostAddress());
                    if(session.getPassword().equals(JsonHelper.getString(request, session_password.name()))) {
                        String deviceName = request.getString(name.name());
                        String ipAddress = request.getString(ip_address.name());
                        Operation vOpe = Operation.valueOf(request.getString(ope.name()));
                        device = databaseManager.getDevice(sessionToken, deviceName, ipAddress);
                        if(vOpe.equals(CONNECT)) {
                            boolean connect = false;
                            if(session.isSingleUseMode()) {
                                if(databaseManager.getDevices(sessionToken, false).size() < 1)
                                    connect = true;
                                else
                                    sendErrorResponse();
                            } else
                                connect = true;
                            if((connect && (device == null || !device.isBlacklisted()))) {
                                // TODO: 03/01/2023 payload:
                                //  device name
                                //  ip
                                //  type
                                //  sPassword
                                databaseManager.insertNewDevice(sessionToken, deviceName, ipAddress,
                                        currentTimeMillis(), Device.Type.valueOf(request.getString(type.name())));
                                // TODO: 03/01/2023 response
                                // status code
                                //  session - less the password
                                //  passwords
                                //  devices
                                JSONObject jSession = session.toJSON();
                                jSession.remove(password.name());
                                sendSuccessfulResponse(response.put(Session.SessionKeys.session.name(), jSession)
                                        .put(passwords.name(), databaseManager.getPasswords(sessionToken, false))
                                        .put(devices.name(), databaseManager.getDevices(sessionToken, false)));
                            } else
                                sendErrorResponse();
                        } else {
                            if(!device.isBlacklisted()) {
                                switch (vOpe) {
                                    case CREATE_PASSWORD -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  sPassword
                                        //  tail
                                        //  scopes
                                        //  length
                                        int length = request.getInt(PasswordKeys.length.name());
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
                                            databaseManager.insertNewPassword(sessionToken, request.getString(tail.name()),
                                                    fetchScopes(request.getJSONArray(scopes.name())), password.toString());
                                            // TODO: 03/01/2023 response
                                            // status code
                                            //  password
                                            sendSuccessfulResponse(response.put(PasswordKeys.password.name(), password));
                                        } else
                                            sendErrorResponse();
                                    }
                                    case INSERT_PASSWORD -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
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
                                                databaseManager.insertNewPassword(sessionToken, tail,
                                                        fetchScopes(request.getJSONArray(scopes.name())), password);
                                                // TODO: 03/01/2023 response
                                                // status code
                                                sendSuccessfulResponse(response);
                                            } else
                                                sendErrorResponse();
                                        } else
                                            sendErrorResponse();
                                    }
                                    case DELETE_PASSWORD -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  sPassword
                                        //  tail
                                        String tail = request.getString(PasswordKeys.tail.name());
                                        Password password = databaseManager.getPassword(sessionToken, tail);
                                        if(password != null) {
                                            if(password.getStatus().equals(ACTIVE))
                                                databaseManager.deletePassword(sessionToken, tail);
                                            else
                                                databaseManager.permanentlyDeletePassword(sessionToken, tail);
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case RECOVER_PASSWORD -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  sPassword
                                        //  tail
                                        String tail = request.getString(PasswordKeys.tail.name());
                                        Password password = databaseManager.getPassword(sessionToken, tail);
                                        if(password != null && password.getStatus().equals(DELETED)) {
                                            databaseManager.recoverPassword(sessionToken, tail);
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case ADD_SCOPE -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  sPassword
                                        //  tail
                                        // add_scope
                                        String tail = request.getString(PasswordKeys.tail.name());
                                        Password password = databaseManager.getPassword(sessionToken, tail);
                                        if(password != null) {
                                            databaseManager.addPasswordScope(sessionToken, password,
                                                    request.getString(scope.name()));
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case EDIT_SCOPE -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  sPassword
                                        //  tail
                                        // edit_scope
                                        // old_scope
                                        String tail = request.getString(PasswordKeys.tail.name());
                                        Password password = databaseManager.getPassword(sessionToken, tail);
                                        if(password != null) {
                                            databaseManager.editPasswordScope(sessionToken, password,
                                                    request.getString(old_scope.name()), request.getString(scope.name()));
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case REMOVE_SCOPE -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  sPassword
                                        //  tail
                                        // remove_scope
                                        String tail = request.getString(PasswordKeys.tail.name());
                                        Password password = databaseManager.getPassword(sessionToken, tail);
                                        if(password != null) {
                                            databaseManager.removePasswordScope(sessionToken, password,
                                                    request.getString(scope.name()));
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case DISCONNECT -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  type
                                        // target_device:
                                        //  - device name
                                        //  - ip
                                        if(JsonHelper.getJSONObject(request, target_device.name()) != null) {
                                            request = request.getJSONObject(target_device.name());
                                            device = databaseManager.getDevice(sessionToken, request.getString(name.name()),
                                                    request.getString(ip_address.name()));
                                        }
                                        if(device != null) {
                                            databaseManager.deleteDevice(sessionToken, device.getName(), device.getIpAddress());
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case MANAGE_DEVICE_AUTHORIZATION -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  type
                                        // target_device:
                                        //  - device name
                                        //  - ip
                                        request = request.getJSONObject(target_device.name());
                                        device = databaseManager.getDevice(sessionToken, request.getString(name.name()),
                                                request.getString(ip_address.name()));
                                        if(device != null) {
                                            if(device.isBlacklisted()) {
                                                databaseManager.unblacklistDevice(sessionToken, device.getName(),
                                                        device.getIpAddress());
                                            } else {
                                                databaseManager.blacklistDevice(sessionToken, device.getName(),
                                                        device.getIpAddress());
                                            }
                                            // TODO: 03/01/2023 response
                                            // status code
                                            sendSuccessfulResponse(response);
                                        } else
                                            sendErrorResponse();
                                    }
                                    case DELETE_ACCOUNT -> {
                                        // TODO: 03/01/2023 payload:
                                        //  device name
                                        //  ip
                                        //  type
                                        databaseManager.deleteSession(session);
                                        session = null;
                                        // TODO: 03/01/2023 response AND CLOSE SOCKETMANAGER ALL LISTENERS WITH THE SPECIFIC METHOD
                                        // status code
                                        sendSuccessfulResponse(response);
                                    }
                                    default -> sendErrorResponse();
                                }
                            } else
                                sendErrorResponse();
                        }
                    } else
                        sendErrorResponse();
                } catch (Exception e) {
                    e.printStackTrace();
                    sendErrorResponse();
                } finally {
                    response.clear();
                }
            }
            System.out.println("This session has been deleted");
        });
    }

    /**
     * Method to send a successful response to the client
     * @param message: message to send
     *
     * @throws Exception when an error occurred
     **/
    @Wrapper
    private void sendSuccessfulResponse(JSONObject message) throws Exception {
        sendResponse(message.put(status_code.name(), SUCCESSFUL_STATUS));
    }

    /**
     * Method to send an error response to the client <br>
     * Any params required
     * **/
    @Wrapper
    private void sendErrorResponse() {
        try {
            sendResponse(new JSONObject().put(status_code.name(), ERROR_STATUS));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Method to send a response to the client
     * @param message: message to send
     *
     * @throws Exception when an error occurred
     **/
    private void sendResponse(JSONObject message) throws Exception {
        socketManager.writeContent(serverCipher.encryptResponse(message.toString()));
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
