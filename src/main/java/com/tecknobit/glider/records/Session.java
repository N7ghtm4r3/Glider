package com.tecknobit.glider.records;

import com.tecknobit.apimanager.apis.encryption.aes.ClientCipher;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

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

    private final String ivSpec;
    private final String secretKey;
    private final String password;
    private final String hostAddress;
    private final int hostPort;
    private final boolean singleUseMode;
    private final boolean QRCodeLoginEnabled;

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
    public String getIvSpec() {
        return ivSpec;
    }

    public IvParameterSpec getOIvSpec() {
        return ClientCipher.createIvParameter(ivSpec);
    }

    public String getSecretKey() {
        return secretKey;
    }

    public SecretKey getOSecretKey() {
        return ClientCipher.createSecretKey(secretKey);
    }

    public String getPassword() {
        return password;
    }

    public String getHostAddress() {
        return hostAddress;
    }

    public int getHostPort() {
        return hostPort;
    }

    public boolean isSingleUseMode() {
        return singleUseMode;
    }

    public boolean isQRCodeLoginEnabled() {
        return QRCodeLoginEnabled;
    }

}
