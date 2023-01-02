package com.tecknobit.glider.records;

import com.tecknobit.apimanager.apis.encryption.aes.ClientCipher;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 * The {@link Session} is class useful to store all the information for a {@code Glider}'s session
 * allowing the correct workflow
 *
 * @author Tecknobit - N7ghtm4r3
 * @see GliderRecord
 **/
public class Session extends GliderRecord {

    /**
     * {@code TOKEN_KEY} host address key
     */
    public static final String TOKEN_KEY = "token";

    /**
     * {@code IV_SPEC_KEY} iv parameters spec key
     */
    public static final String IV_SPEC_KEY = "iv_spec";

    /**
     * {@code SECRET_KEY} secret key
     */
    public static final String SECRET_KEY = "secret_key";

    /**
     * {@code HOST_ADDRESS_KEY} host address key
     */
    public static final String HOST_ADDRESS_KEY = "host_address";

    /**
     * {@code HOST_PORT_KEY} host port key
     */
    public static final String HOST_PORT_KEY = "host_port";

    /**
     * {@code SINGLE_USE_MODE_KEY} single use mode key
     */
    public static final String SINGLE_USE_MODE_KEY = "single_use_mode";

    /**
     * {@code QR_CODE_LOGIN_KEY} qr code login key
     */
    public static final String QR_CODE_LOGIN_KEY = "qr_code_login";

    /**
     * {@code ivSpec} {@link IvParameterSpec} of the session
     **/
    private final String ivSpec;

    /**
     * {@code secretKey} {@link SecretKey} of the session
     **/
    private final String secretKey;

    /**
     * {@code password} of the sessions
     **/
    private final String password;

    /**
     * {@code hostAddress} host address of the session
     **/
    private final String hostAddress;

    /**
     * {@code hostPort} host port of the session
     **/
    private final int hostPort;

    /**
     * {@code singleUseMode} whether the session allows multiple connections, so multiple devices
     **/
    private final boolean singleUseMode;

    /**
     * {@code QRCodeLoginEnabled} whether the session allows login by QR-CODE method
     **/
    private final boolean QRCodeLoginEnabled;

    /**
     * Constructor to init {@link Session} object
     *
     * @param token: session token value
     * @param ivSpec:     {@link IvParameterSpec} of the session
     * @param secretKey:    {@link SecretKey} of the session
     * @param password: password of the session
     * @param hostAddress:   host address of the session
     * @param hostPort: host port of the session
     * @param singleUseMode:   whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled:   whether the session allows login by QR-CODE method
     **/
    public Session(String token, String ivSpec, String secretKey, String password, String hostAddress, int hostPort,
                   boolean singleUseMode, boolean QRCodeLoginEnabled) {
        super(token);
        this.ivSpec = ivSpec;
        this.secretKey = secretKey;
        this.password = password;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.singleUseMode = singleUseMode;
        this.QRCodeLoginEnabled = QRCodeLoginEnabled;
    }

    /**
     * Method to get {@link #ivSpec} instance <br>
     * Any params required
     *
     * @return {@link #ivSpec} instance as {@link String}
     **/
    public String getIvSpec() {
        return ivSpec;
    }

    /**
     * Method to get {@link #ivSpec} instance <br>
     * Any params required
     *
     * @return {@link #ivSpec} instance as {@link IvParameterSpec}
     **/
    public IvParameterSpec getOIvSpec() {
        return ClientCipher.createIvParameter(ivSpec);
    }

    /**
     * Method to get {@link #secretKey} instance <br>
     * Any params required
     *
     * @return {@link #secretKey} instance as {@link String}
     **/
    public String getSecretKey() {
        return secretKey;
    }

    /**
     * Method to get {@link #secretKey} instance <br>
     * Any params required
     *
     * @return {@link #secretKey} instance as {@link SecretKey}
     **/
    public SecretKey getOSecretKey() {
        return ClientCipher.createSecretKey(secretKey);
    }

    /**
     * Method to get {@link #password} instance <br>
     * Any params required
     *
     * @return {@link #password} instance as {@link String}
     **/
    public String getPassword() {
        return password;
    }

    /**
     * Method to get {@link #hostAddress} instance <br>
     * Any params required
     *
     * @return {@link #hostAddress} instance as {@link String}
     **/
    public String getHostAddress() {
        return hostAddress;
    }

    /**
     * Method to get {@link #hostPort} instance <br>
     * Any params required
     *
     * @return {@link #hostPort} instance as int
     **/
    public int getHostPort() {
        return hostPort;
    }

    /**
     * Method to get {@link #singleUseMode} instance <br>
     * Any params required
     *
     * @return {@link #singleUseMode} instance as boolean
     **/
    public boolean isSingleUseMode() {
        return singleUseMode;
    }

    /**
     * Method to get {@link #QRCodeLoginEnabled} instance <br>
     * Any params required
     *
     * @return {@link #QRCodeLoginEnabled} instance as boolean
     **/
    public boolean isQRCodeLoginEnabled() {
        return QRCodeLoginEnabled;
    }

}
