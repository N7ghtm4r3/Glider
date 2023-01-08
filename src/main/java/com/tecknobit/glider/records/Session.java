package com.tecknobit.glider.records;

import com.tecknobit.apimanager.apis.encryption.aes.ClientCipher;
import com.tecknobit.apimanager.formatters.JsonHelper;
import org.json.JSONObject;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static com.tecknobit.glider.records.Session.SessionKeys.*;

/**
 * The {@link Session} is class useful to store all the information for a {@code Glider}'s session
 * allowing the correct workflow
 *
 * @author Tecknobit - N7ghtm4r3
 * @see GliderRecord
 **/
public class Session {

    /**
     * {@code SessionKeys} list of available keys for the session
     **/
    public enum SessionKeys {

        /**
         * {@code session} key of the session
         **/
        session,

        /**
         * {@code token} key of the session
         **/
        token,

        /**
         * {@code iv_spec} key of the session
         **/
        iv_spec,

        /**
         * {@code secret_key} key of the session
         **/
        secret_key,

        /**
         * {@code session_password} sessionPassword key of the session
         **/
        session_password,

        /**
         * {@code host_address} key of the session
         **/
        host_address,

        /**
         * {@code host_port} key of the session
         **/
        host_port,

        /**
         * {@code single_use_mode} key of the session
         **/
        single_use_mode,

        /**
         * {@code qr_code_login} key of the session
         **/
        qr_code_login,

        /**
         * {@code run_in_localhost} key of the session
         **/
        run_in_localhost

    }

    /**
     * {@code token} of the session
     **/
    private final String token;

    /**
     * {@code ivSpec} {@link IvParameterSpec} of the session
     **/
    private final String ivSpec;

    /**
     * {@code secretKey} {@link SecretKey} of the session
     **/
    private final String secretKey;

    /**
     * {@code sessionPassword} password of the sessions
     **/
    private final String sessionPassword;

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
     * {@code runInLocalhost} whether the session run only in localhost
     **/
    private final boolean runInLocalhost;

    /**
     * Constructor to init {@link Session} object
     *
     * @param token              : session token value
     * @param ivSpec             :     {@link IvParameterSpec} of the session
     * @param secretKey          :    {@link SecretKey} of the session
     * @param sessionPassword    : session password of the session
     * @param hostAddress        :   host address of the session
     * @param hostPort           : host port of the session
     * @param singleUseMode      :   whether the session allows multiple connections, so multiple devices
     * @param QRCodeLoginEnabled :   whether the session allows login by QR-CODE method
     * @param runInLocalhost     : whether the session run only in localhost
     **/
    public Session(String token, String ivSpec, String secretKey, String sessionPassword, String hostAddress,
                   int hostPort, boolean singleUseMode, boolean QRCodeLoginEnabled, boolean runInLocalhost) {
        this.token = token;
        this.ivSpec = ivSpec;
        this.secretKey = secretKey;
        this.sessionPassword = sessionPassword;
        this.hostAddress = hostAddress;
        this.hostPort = hostPort;
        this.singleUseMode = singleUseMode;
        this.QRCodeLoginEnabled = QRCodeLoginEnabled;
        this.runInLocalhost = runInLocalhost;
    }

    /**
     * Constructor to init {@link Session} object
     *
     * @param jSession : session details as {@link JSONObject}
     **/
    public Session(JSONObject jSession) {
        JsonHelper hSession = new JsonHelper(jSession);
        token = hSession.getString(SessionKeys.token.name());
        ivSpec = hSession.getString(iv_spec.name());
        secretKey = hSession.getString(secret_key.name());
        sessionPassword = hSession.getString(session_password.name());
        hostAddress = hSession.getString(host_address.name());
        hostPort = hSession.getInt(host_port.name());
        singleUseMode = hSession.getBoolean(single_use_mode.name());
        QRCodeLoginEnabled = hSession.getBoolean(qr_code_login.name());
        runInLocalhost = hSession.getBoolean(run_in_localhost.name());
    }

    /**
     * Method to get {@link #token} instance <br>
     * Any params required
     *
     * @return {@link #token} instance as {@link String}
     **/
    public String getToken() {
        return token;
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
     * Method to get {@link #sessionPassword} instance <br>
     * Any params required
     *
     * @return {@link #sessionPassword} instance as {@link String}
     **/
    public String getSessionPassword() {
        return sessionPassword;
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

    /**
     * Method to get {@link #runInLocalhost} instance <br>
     * Any params required
     *
     * @return {@link #runInLocalhost} instance as boolean
     **/
    public boolean runInLocalhost() {
        return runInLocalhost;
    }

    /**
     * Returns a string representation of the object <br>
     * Any params required
     *
     * @return a string representation of the object as {@link JSONObject}
     */
    public JSONObject toJSON() {
        return new JSONObject(this);
    }

    /**
     * Returns a string representation of the object <br>
     * Any params required
     *
     * @return a string representation of the object as {@link String}
     */
    @Override
    public String toString() {
        return new JSONObject(this).toString();
    }

}
