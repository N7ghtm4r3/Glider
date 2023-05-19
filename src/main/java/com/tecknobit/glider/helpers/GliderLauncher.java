package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.annotations.Wrapper;
import com.tecknobit.apimanager.apis.ConsolePainter;
import com.tecknobit.apimanager.apis.QRCodeHelper;
import com.tecknobit.apimanager.apis.SocketManager;
import com.tecknobit.apimanager.exceptions.SaveData;
import com.tecknobit.apimanager.formatters.JsonHelper;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Device.DevicePermission;
import com.tecknobit.glider.records.Password;
import com.tecknobit.glider.records.Session;
import com.tecknobit.glider.records.Session.SessionKeys;
import org.json.JSONObject;

import javax.crypto.BadPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import java.io.*;
import java.net.BindException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static com.tecknobit.apimanager.apis.ConsolePainter.ANSIColor.GREEN;
import static com.tecknobit.apimanager.apis.ConsolePainter.ANSIColor.RED;
import static com.tecknobit.apimanager.apis.SocketManager.StandardResponseCode.*;
import static com.tecknobit.apimanager.apis.encryption.aes.ClientCipher.Algorithm.CBC_ALGORITHM;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCIvParameterSpecString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCSecretKeyString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.GenericServerCipher.KeySize.k256;
import static com.tecknobit.apimanager.formatters.TimeFormatter.getStringDate;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.devices;
import static com.tecknobit.glider.helpers.DatabaseManager.Table.passwords;
import static com.tecknobit.glider.helpers.GliderLauncher.GliderKeys.*;
import static com.tecknobit.glider.helpers.GliderLauncher.Operation.*;
import static com.tecknobit.glider.records.Device.DeviceKeys.*;
import static com.tecknobit.glider.records.Device.DevicePermission.*;
import static com.tecknobit.glider.records.Password.*;
import static com.tecknobit.glider.records.Password.PasswordKeys.*;
import static com.tecknobit.glider.records.Password.Status.ACTIVE;
import static com.tecknobit.glider.records.Password.Status.DELETED;
import static com.tecknobit.glider.records.Session.SessionKeys.*;
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
         * {@code CHANGE_DEVICE_PERMISSION} change the device permission choosing between {@link DevicePermission}
         */
        CHANGE_DEVICE_PERMISSION,

        /**
         * {@code DELETE_SESSION} session deletion operation
         */
        DELETE_SESSION,

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
         * {@code "databasePath"} key
         */
        databasePath

    }

    /**
     * {@code BackupInterval} list of available backup intervals to use to schedule your backups
     */
    public enum BackupInterval {

        /**
         * {@code FIVE_MINUTES} the backup of the database will be executed each five minutes
         */
        FIVE_MINUTES(5),

        /**
         * {@code FIFTEEN_MINUTES} the backup of the database will be executed each fifteen minutes
         */
        FIFTEEN_MINUTES(15),

        /**
         * {@code HALF_HOUR} the backup of the database will be executed each half hours
         */
        HALF_HOUR(30),

        /**
         * {@code ONE_HOUR} the backup of the database will be executed each one hour
         */
        ONE_HOUR(60),

        /**
         * {@code FOUR_HOURS} the backup of the database will be executed each four hours
         */
        FOUR_HOURS(240),

        /**
         * {@code EIGHT_HOURS} the backup of the database will be executed each eight hours
         */
        EIGHT_HOURS(480),

        /**
         * {@code TWELVE_HOURS} the backup of the database will be executed each twelve hours
         */
        TWELVE_HOURS(720),

        /**
         * {@code ONE_DAY} the backup of the database will be executed each one day
         */
        ONE_DAY(1440),

        /**
         * {@code ONE_WEEK} the backup of the database will be executed each one week
         */
        ONE_WEEK(10080),

        /**
         * {@code ONE_MONTH} the backup of the database will be executed each one month
         */
        ONE_MONTH(40320);

        /**
         * {@code interval} value to use
         */
        private final long interval;

        /**
         * Constructor to init {@link BackupInterval} object
         *
         * @param interval: interval value to use
         **/
        BackupInterval(long interval) {
            this.interval = interval * 60 * 1000;
        }

        /**
         * Method to get {@link #interval} instance <br>
         * No-any params required
         *
         * @return {@link #interval} instance as long
         **/
        public long getInterval() {
            return interval;
        }

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
    private DatabaseManager databaseManager;

    /**
     * {@code socketManager} manager of the socket communication
     */
    private SocketManager socketManager;

    /**
     * {@code session} of the {@code Glider}'s service
     */
    private Session session;

    /**
     * {@code hostPort} host port for {@link #session} of the {@code Glider}'s service
     */
    private int hostPort;

    /**
     * {@code qrCodeHelper} instance to manage the QRCode login procedure
     */
    private QRCodeHelper qrCodeHelper;

    /**
     * {@code publicIvSpec} the public instantiation vector
     */
    private String publicIvSpec;

    /**
     * {@code publicCipherKey} the public secret key for the cipher
     */
    private String publicCipherKey;

    /**
     * {@code consolePainter} useful to color the output console
     */
    private final ConsolePainter consolePainter = new ConsolePainter();

    /**
     * Constructor to init a {@link GliderLauncher} object <br>
     * No-any params required
     *
     * @apiNote this constructor is used to automatically start the {@code Glider}'s service from a JSON configuration
     * file, you can before set that <a href="https://github.com/N7ghtm4r3/Glider/tree/main/documd/GliderBackend.md">file</a>
     * and then invoke this constructor or execute directly the JAR file created by us to run directly the {@code Glider}'s
     * backend service
     * @implSpec <b>you need at least the Java 18 JDK installed on your machine</b>
     */
    public GliderLauncher() throws Exception {
        try {
            File fConfigs = new File("glider_configs.json");
            JSONObject configs = new JSONObject(new Scanner(fConfigs).useDelimiter("\\Z").next());
            JsonHelper hConfigs = new JsonHelper(configs);
            if (hConfigs.getJSONObject(SessionKeys.session.name()) != null)
                invokeRunningMode(configs);
            else {
                hConfigs.setJSONObjectSource(hConfigs.getJSONObject("configuration", new JSONObject()));
                try {
                    boolean bRunInLocalhost = hConfigs.getBoolean(runInLocalhost.name());
                    setConfigurationMode(hConfigs.getString(databasePath.name()),
                            hConfigs.getString(password.name()),
                            hConfigs.getBoolean(singleUseMode.name()),
                            hConfigs.getBoolean(QRCodeLoginEnabled.name()),
                            hConfigs.getString(hostAddress.name(), new SocketManager(false)
                                    .getHost(!bRunInLocalhost)),
                            hConfigs.getInt(SessionKeys.hostPort.name()),
                            bRunInLocalhost);
                } catch (SaveData e) {
                    JSONObject session = new JSONObject(e.getLocalizedMessage().replace("Note: is not an error, but is an alert!\n" +
                            "Please you should safely save: ", ""));
                    configs.getJSONObject("glider").put(SessionKeys.session.name(), session);
                    try (FileWriter writer = new FileWriter(fConfigs, false)) {
                        writer.write(configs.toString(4));
                    }
                    invokeRunningMode(configs);
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
            throw new Exception("glider_configs.json not found, cannot continue");
        }
    }

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
        setRunningMode(databasePath, token, ivSpec, secretKey);
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
        this(databasePath, password, singleUseMode, QRCodeLoginEnabled, new SocketManager(false)
                .getHost(!runInLocalhost), hostPort, runInLocalhost);
    }

    /**
     * Constructor to init {@link GliderLauncher} object
     *
     * @param databasePath:       path where create the database
     * @param password:           password to protect the {@link Session}
     * @param singleUseMode:      whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled: whether the session allows login by QR-CODE method
     *                            (if enabled will be shown on {@code "hostAddress:(hostPort + 1)"})
     * @param hostAddress:        host address of the session
     * @param hostPort:           host port of the session
     * @param runInLocalhost:     whether the session can accept requests outside localhost
     * @throws SaveData to safe the {@link Session}'s data
     * @apiNote this constructor is used to create a new {@link Session} if you need to start the {@code Glider}'s
     * service for the first time
     **/
    public GliderLauncher(String databasePath, String password, boolean singleUseMode, boolean QRCodeLoginEnabled,
                          String hostAddress, int hostPort, boolean runInLocalhost) throws Exception {
        setConfigurationMode(databasePath, password, singleUseMode, QRCodeLoginEnabled, hostAddress, hostPort,
                runInLocalhost);
    }

    /**
     * Method to invoke the {@link #setRunningMode(String, String, String, String)} method and start the service
     *
     * @param configs: configs details to use for the {@code Glider}'s service
     */
    private void invokeRunningMode(JSONObject configs) throws Exception {
        JsonHelper hConfigs = new JsonHelper(configs);
        setRunningMode(hConfigs.getString(databasePath.name()),
                hConfigs.getString(token.name()),
                hConfigs.getString(ivSpec.name()),
                hConfigs.getString(secretKey.name()));
        startService();
        String backupInterval = hConfigs.getString("backupInterval");
        if (backupInterval != null) {
            try {
                execDatabaseBackup(hConfigs.getString("backupPath"), BackupInterval.valueOf(backupInterval));
            } catch (IllegalArgumentException i) {
                stopService();
                throw new Exception("Not a valid interval inserted for the schedule of the backup of the database");
            }
        }
    }

    /**
     * Method to configure the {@code Glider}'s service
     *
     * @param databasePath:       path where create the database
     * @param password:           password to protect the {@link Session}
     * @param singleUseMode:      whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled: whether the session allows login by QR-CODE method
     *                            (if enabled will be shown on {@code "hostAddress:(hostPort + 1)"})
     * @param hostAddress:        host address of the session
     * @param hostPort:           host port of the session
     * @param runInLocalhost:     whether the session can accept requests outside localhost
     * @throws SaveData to safe the {@link Session}'s data
     * @apiNote this constructor is used to create a new {@link Session} if you need to start the {@code Glider}'s
     * service for the first time
     **/
    private void setConfigurationMode(String databasePath, String password, boolean singleUseMode,
                                      boolean QRCodeLoginEnabled, String hostAddress, int hostPort,
                                      boolean runInLocalhost) throws Exception {
        if (databasePath.isBlank() || password.isBlank())
            throw new IllegalArgumentException("You must fill the required fields as the database path and the password");
        if (!databasePath.endsWith(".db"))
            databasePath += ".db";
        databaseManager = new DatabaseManager(databasePath);
        String ivSpec = createCBCIvParameterSpecString();
        String secretKey = createCBCSecretKeyString(k256);
        String token = UUID.randomUUID().toString().replaceAll("-", "");
        databaseManager.insertNewSession(token, ivSpec, secretKey, password, hostAddress, hostPort, singleUseMode,
                QRCodeLoginEnabled, runInLocalhost);
        throw new SaveData("\n" + new JSONObject().put(SessionKeys.ivSpec.name(), ivSpec)
                .put(SessionKeys.secretKey.name(), secretKey)
                .put(SessionKeys.token.name(), token)
                .put(GliderKeys.databasePath.name(), databasePath)
                .toString(4));
    }

    /**
     * Method to configure the running mode for the {@code Glider}'s service
     *
     * @param databasePath: path where the database has been created
     * @param token:        session token value
     * @param ivSpec:       {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     **/
    private void setRunningMode(String databasePath, String token, String ivSpec, String secretKey) throws Exception {
        databaseManager = new DatabaseManager(databasePath);
        session = databaseManager.getSession(token, ivSpec, secretKey);
        if (session == null)
            throw new Exception("No-any sessions found with that token, retry");
        socketManager = new SocketManager(false, session.getIvSpec(), session.getSecretKey(), CBC_ALGORITHM);
        this.hostPort = session.getHostPort();
        if (session.isQRCodeLoginEnabled()) {
            qrCodeHelper = new QRCodeHelper();
            try {
                qrCodeHelper.hostQRCode(session.getHostPort() + 1, new JSONObject()
                                .put(hostAddress.name(), session.getHostAddress())
                                .put(SessionKeys.hostPort.name(), hostPort)
                                .put(SessionKeys.token.name(), "Glider"),
                        "Glider.png",
                        250,
                        true,
                        getResourceFile("qrcode", "html"));
            } catch (BindException e) {
                consolePainter.printBold("You cannot have multiple sessions on the same port at the same time", RED);
                e.printStackTrace();
                System.exit(1);
            }
        } else
            qrCodeHelper = null;
    }

    /**
     * Method to get a file from the resources folder
     *
     * @param prefix: prefix of the file to fetch
     * @param suffix: suffix of the file to fetch
     * @return file from the resources folder as {@link File}
     */
    private File getResourceFile(String prefix, String suffix) throws IOException {
        File file = File.createTempFile(prefix, suffix);
        file.deleteOnExit();
        InputStreamReader inputStreamReader = new InputStreamReader(Objects.requireNonNull(this.getClass()
                .getClassLoader().getResourceAsStream(prefix + "." + suffix)));
        try (FileWriter fileWriter = new FileWriter(file)) {
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while ((line = bufferedReader.readLine()) != null)
                fileWriter.write(line);
        }
        return file;
    }

    /**
     * Method to start the {@code Glider}'s service with the details
     * fetched from the database if already exist or with that inserted at the
     * first run <br>
     * No-any params required
     *
     * @throws IOException when an error occurred during {@link #socketManager}'s workflow
     **/
    public void startService() throws IOException {
        JSONObject response = new JSONObject();
        socketManager.setDefaultErrorResponse(new JSONObject().put(statusCode.name(), FAILED));
        socketManager.startListener(hostPort, () -> {
            JSONObject request;
            Device device, clientDevice;
            while (socketManager.continueListening()) {
                consolePainter.printBold("Waiting...");
                try {
                    String ipAddress = SocketManager.getIpAddress(socketManager.acceptRequest());
                    try {
                        request = new JSONObject(socketManager.readContent());
                    } catch (IllegalArgumentException | BadPaddingException e) {
                        String privateSecretKey = session.getSecretKey();
                        if (socketManager.getCipherKey().equals(privateSecretKey))
                            socketManager.changeCipherKeys(publicIvSpec, publicCipherKey);
                        else
                            socketManager.changeCipherKeys(session.getIvSpec(), privateSecretKey);
                        try {
                            request = new JSONObject(socketManager.readLastContent());
                        } catch (IllegalArgumentException | BadPaddingException eP) {
                            socketManager.writePlainContent(new JSONObject().put(ivSpec.name(), publicIvSpec)
                                    .put(secretKey.name(), publicCipherKey));
                            request = null;
                        }
                    }
                    if(request != null) {
                        boolean check = true;
                        if (session.runInLocalhost()) {
                            StringBuilder address = new StringBuilder(ipAddress);
                            address.reverse();
                            int j = 0;
                            for (; j < address.length(); j++)
                                if (address.charAt(j) == '.')
                                    break;
                            address.replace(0, j, "").reverse();
                            check = session.getHostAddress().startsWith(address.toString());
                        }
                        if (check && session.getSessionPassword().equals(JsonHelper.getString(request,
                                sessionPassword.name()))) {
                            String deviceName = request.getString(name.name());
                            Operation vOpe = Operation.valueOf(request.getString(ope.name()));
                            device = databaseManager.getDevice(session, deviceName);
                            if (vOpe.equals(GET_PUBLIC_KEYS)) {
                                socketManager.writePlainContent(new JSONObject().put(ivSpec.name(), publicIvSpec)
                                        .put(secretKey.name(), publicCipherKey));
                            } else if (vOpe.equals(CONNECT)) {
                                boolean connect = false;
                                int currentDevices = databaseManager.getDevices(session, false).size();
                                if(session.isSingleUseMode()) {
                                    if (currentDevices < 1)
                                        connect = true;
                                    else
                                        socketManager.sendDefaultErrorResponse();
                                } else
                                    connect = true;
                                if ((connect && (device == null || !device.isBlacklisted()))) {
                                    if (device == null) {
                                        DevicePermission permission = SIMPLE_USER;
                                        if (currentDevices == 0)
                                            permission = ADMIN;
                                        databaseManager.insertNewDevice(session, deviceName, ipAddress, currentTimeMillis(),
                                                Device.Type.valueOf(request.getString(type.name())), permission);
                                    }
                                    sendAllData(response, true, device);
                                    socketManager.changeCipherKeys(session.getIvSpec(), session.getSecretKey());
                                } else
                                    socketManager.sendDefaultErrorResponse();
                            } else if (vOpe.equals(REFRESH_DATA)) {
                                if (device != null) {
                                    if (!device.isBlacklisted())
                                        sendAllData(response, false, device);
                                    else
                                        socketManager.sendDefaultErrorResponse();
                                } else
                                    socketManager.writeContent(response.put(statusCode.name(), GENERIC_RESPONSE));
                            } else {
                                if (!device.isBlacklisted()) {
                                    databaseManager.updateLastActivity(device, ipAddress);
                                    switch (vOpe) {
                                        case CREATE_PASSWORD -> {
                                            int length = request.getInt(Password.PasswordKeys.length.name());
                                            if (device.isPasswordManager() && (length >= PASSWORD_MIN_LENGTH
                                                    && length <= PASSWORD_MAX_LENGTH)) {
                                                ArrayList<Integer> letters = new ArrayList<>();
                                                StringBuilder password = new StringBuilder();
                                                for (int j = 0; j < length; j++) {
                                                    int index;
                                                    do {
                                                        index = new Random().nextInt(122);
                                                        if (index < 33)
                                                            index = 33;
                                                    } while (letters.contains(index) || index == 44);
                                                    letters.add(index);
                                                    password.append(((char) index));
                                                }
                                                databaseManager.insertNewPassword(session, request.getString(tail.name()),
                                                        fetchScopes(request.getJSONArray(scopes.name())), password.toString());
                                                sendSuccessfulResponse(response.put(PasswordKeys.password.name(), password));
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case INSERT_PASSWORD -> {
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            int length = tail.length();
                                            if (device.isPasswordManager() && (length > 0 && length < 30)) {
                                                String password = request.getString(PasswordKeys.password.name());
                                                length = password.length();
                                                if (length >= PASSWORD_MIN_LENGTH && length <= PASSWORD_MAX_LENGTH) {
                                                    databaseManager.insertNewPassword(session, tail,
                                                            fetchScopes(request.getJSONArray(scopes.name())), password);
                                                    sendSuccessfulResponse(response);
                                                } else
                                                    socketManager.sendDefaultErrorResponse();
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case DELETE_PASSWORD -> {
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if (device.isPasswordManager() && password != null) {
                                                if (password.getStatus().equals(ACTIVE))
                                                    databaseManager.deletePassword(session, tail);
                                                else
                                                    databaseManager.permanentlyDeletePassword(session, tail);
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case RECOVER_PASSWORD -> {
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if (device.isPasswordManager() && password != null
                                                    && password.getStatus().equals(DELETED)) {
                                                databaseManager.recoverPassword(session, tail);
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case ADD_SCOPE -> {
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if (device.isPasswordManager() && password != null) {
                                                databaseManager.addPasswordScope(session, password,
                                                        request.getString(scope.name()));
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case EDIT_SCOPE -> {
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if (device.isPasswordManager() && password != null) {
                                                databaseManager.editPasswordScope(session, password,
                                                        request.getString(oldScope.name()), request.getString(scope.name()));
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case REMOVE_SCOPE -> {
                                            String tail = request.getString(PasswordKeys.tail.name());
                                            Password password = databaseManager.getPassword(session, tail);
                                            if (device.isPasswordManager() && password != null) {
                                                databaseManager.removePasswordScope(session, password,
                                                        request.getString(scope.name()));
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case DISCONNECT -> {
                                            clientDevice = device;
                                            if (JsonHelper.getJSONObject(request, targetDevice.name()) != null) {
                                                if (clientDevice.isAccountManager()) {
                                                    request = request.getJSONObject(targetDevice.name());
                                                    device = databaseManager.getDevice(session, request.getString(name.name()));
                                                    if (device.isAdmin() && !clientDevice.isAdmin())
                                                        device = null;
                                                } else
                                                    device = null;
                                            }
                                            if (device != null) {
                                                assignNewAdmin(clientDevice);
                                                databaseManager.deleteDevice(session, device.getName());
                                                sendSuccessfulResponse(response);
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case MANAGE_DEVICE_AUTHORIZATION -> {
                                            clientDevice = device;
                                            if (clientDevice.isAccountManager()) {
                                                request = request.getJSONObject(targetDevice.name());
                                                device = databaseManager.getDevice(session, request.getString(name.name()));
                                                if (device != null && (!device.isAdmin() || clientDevice.isAdmin())) {
                                                    String mDeviceName = device.getName();
                                                    if (device.isBlacklisted())
                                                        databaseManager.unblacklistDevice(session, mDeviceName);
                                                    else
                                                        databaseManager.blacklistDevice(session, mDeviceName);
                                                    sendSuccessfulResponse(response);
                                                } else
                                                    socketManager.sendDefaultErrorResponse();
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case CHANGE_DEVICE_PERMISSION -> {
                                            clientDevice = device;
                                            if (clientDevice.isAccountManager()) {
                                                request = request.getJSONObject(targetDevice.name());
                                                device = databaseManager.getDevice(session, request.getString(name.name()));
                                                if (device != null && (!device.isAdmin() || clientDevice.isAdmin())) {
                                                    databaseManager.changeDevicePermission(session, device.getName(),
                                                            DevicePermission.valueOf(request.getString(permission.name())));
                                                    sendSuccessfulResponse(response);
                                                } else
                                                    socketManager.sendDefaultErrorResponse();
                                            } else
                                                socketManager.sendDefaultErrorResponse();
                                        }
                                        case DELETE_SESSION -> {
                                            if (device.isAdmin()) {
                                                databaseManager.deleteSession(session);
                                                sendSuccessfulResponse(response);
                                                stopService();
                                            } else
                                                socketManager.sendDefaultErrorResponse();
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
                    try {
                        socketManager.sendDefaultErrorResponse();
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                } finally {
                    response.clear();
                }
            }
            consolePainter.printBold("This session has been deleted", RED);
        });
        refreshPublicKeys();
    }

    /**
     * Method to refresh the public keys to cipher the communication <br>
     * No-any params required
     **/
    private void refreshPublicKeys() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                boolean createKeys;
                while (socketManager.continueListening()) {
                    try {
                        if (!session.isSingleUseMode())
                            createKeys = true;
                        else
                            createKeys = databaseManager.getDevices(session, false).size() < 1;
                        if (createKeys) {
                            publicIvSpec = createCBCIvParameterSpecString();
                            publicCipherKey = createCBCSecretKeyString(k256);
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
     * Method to send all data of a {@link Session}
     *
     * @param response      : response where link {@link Session}
     * @param insertSession : whether insert the {@link #session} details
     * @param device:       the device that has been request the data
     **/
    private void sendAllData(JSONObject response, boolean insertSession, Device device) throws Exception {
        if (insertSession) {
            JSONObject jSession = session.toJSON();
            response.put(SessionKeys.session.name(), jSession);
        }
        sendSuccessfulResponse(response.put(passwords.name(), databaseManager.getPasswords(session, false))
                .put(devices.name(), databaseManager.getDevices(session, false, device.getName()))
                .put(permission.name(), device.getPermission()));
    }

    /**
     * Method to assign a new admin when an admin has been logged out
     *
     * @param device: the current device that has being logged out
     * @throws Exception when an error occurred
     * @apiNote the assign is executed choosing for each {@link DevicePermission} the older {@link Device}
     */
    private void assignNewAdmin(Device device) throws Exception {
        String deviceName = device.getName();
        if (device.isAdmin() && databaseManager.getDevices(session, false, deviceName).size() != 0 &&
                databaseManager.getDevices(session, false, deviceName, ADMIN).size() == 0) {
            Device newAdmin = databaseManager.getOlderDevice(session, ACCOUNT_MANAGER);
            if (newAdmin == null) {
                newAdmin = databaseManager.getOlderDevice(session, PASSWORD_MANAGER);
                if (newAdmin == null)
                    newAdmin = databaseManager.getOlderDevice(session, SIMPLE_USER);
            }
            databaseManager.changeDevicePermission(session, newAdmin, ADMIN);
        }
    }

    /**
     * Method to send a successful response to the client
     *
     * @param message: message to send
     * @throws Exception when an error occurred
     **/
    private void sendSuccessfulResponse(JSONObject message) throws Exception {
        socketManager.writeContent(message.put(statusCode.name(), SUCCESSFUL));
    }

    /**
     * Method to execute a scheduled database's backup
     *
     * @param backupInterval: interval to schedule the backup
     * @throws Exception when an error occurred
     **/
    @Wrapper
    public void execDatabaseBackup(BackupInterval backupInterval) throws Exception {
        execDatabaseBackup(null, backupInterval);
    }

    /**
     * Method to execute a scheduled database's backup
     *
     * @param backupPath:     the path where save the database's backup, if null the backup file will be auto-named
     *                        automatically and will be stored in the same directory of the original database file
     * @param backupInterval: interval to schedule the backup
     * @throws Exception when an error occurred
     **/
    public void execDatabaseBackup(String backupPath, BackupInterval backupInterval) throws Exception {
        AtomicLong previousBackup = new AtomicLong(currentTimeMillis());
        String databasePath = databaseManager.getDatabasePath();
        String databaseName = databasePath.split(".db")[0];
        final boolean autoNameBackup = backupPath == null;
        if (!autoNameBackup) {
            if (!backupPath.endsWith(".db"))
                backupPath += ".db";
            if (backupPath.equals(databasePath)) {
                stopService();
                throw new IllegalStateException("The path for the backup must be different from the original path");
            }
        }
        final String[] lambdaBackupPath = {backupPath};
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            while (socketManager.continueListening()) {
                try {
                    if (System.currentTimeMillis() - previousBackup.get() >= backupInterval.getInterval()) {
                        if (autoNameBackup)
                            lambdaBackupPath[0] = databaseName + "-backup" + System.currentTimeMillis() + ".db";
                        Files.copy(Path.of(databasePath), Path.of(lambdaBackupPath[0]), StandardCopyOption.REPLACE_EXISTING);
                        previousBackup.set(currentTimeMillis());
                        consolePainter.printBold("Backup of [" + getStringDate(previousBackup.get()) +
                                "] executed", GREEN);
                    }
                } catch (NoSuchFileException fileException) {
                    String[] directories = lambdaBackupPath[0].replaceAll("\\\\", "/").split("/");
                    String directoriesPath = lambdaBackupPath[0].replace(directories[directories.length - 1], "");
                    if (!new File(directoriesPath).mkdirs()) {
                        try {
                            throw new Exception(fileException);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            executor.shutdownNow();
        });
    }

    /**
     * Method to stop the {@code Glider}'s service
     * No-any params required
     *
     * @throws Exception when an error occurred
     **/
    private void stopService() throws Exception {
        if (qrCodeHelper != null)
            qrCodeHelper.stopHosting();
        socketManager.stopListener();
    }

    /**
     * Method to get {@link #databaseManager} instance <br>
     * No-any params required
     *
     * @return {@link #databaseManager} instance as {@link DatabaseManager}
     **/
    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    /**
     * Method to get {@link #session} instance <br>
     * No-any params required
     *
     * @return {@link #session} instance as {@link Session}
     **/
    public Session getSession() {
        return session;
    }

}
