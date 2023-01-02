package com.tecknobit.glider.helpers;

import com.tecknobit.apimanager.apis.SocketManager;
import com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher;
import com.tecknobit.glider.records.Device;
import com.tecknobit.glider.records.Session;

import java.io.IOException;
import java.util.UUID;

import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCIvParameterSpecString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.CBCServerCipher.createCBCSecretKeyString;
import static com.tecknobit.apimanager.apis.encryption.aes.serverside.GenericServerCipher.KeySize.k256;

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
     * {@code SERVER_STATUS_KEY} server status key
     */
    public static final String SERVER_STATUS_KEY = "server_status";

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
    private final Session session;
    
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
        socketManager.startListener(hostPort, () -> {
            while (true) {
                System.out.println("Waiting...");
                try {
                    socketManager.acceptRequest();
                    String request = socketManager.readContent();
                    // TODO: 02/01/2023 CREATE AN ERROR METHOD TO SEND WITH THE SOCKETMANAGER 
                    switch (request) {
                        default ->
                            socketManager.writeContent(serverCipher.encryptResponse("ERROR-MESSAGE-TO-CHOSE"));
                    }
                } catch (Exception e) {
                    try {
                        socketManager.writeContent(serverCipher.encryptResponse("ERROR-MESSAGE-TO-CHOSE"));
                    } catch (Exception ex) {
                        throw new RuntimeException(ex);
                    }
                }
            }
        });
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
