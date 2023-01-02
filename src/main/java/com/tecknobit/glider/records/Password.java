package com.tecknobit.glider.records;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * The {@link Password} is class useful to store all the information for a {@code Glider}'s password
 * allowing the correct workflow
 *
 * @author Tecknobit - N7ghtm4r3
 * @see GliderRecord
 **/
public class Password extends GliderRecord {

    /**
     * {@code TAIL_KEY} tail key
     */
    public static final String TAIL_KEY = "tail";

    /**
     * {@code SCOPES_KEY} scopes key
     */
    public static final String SCOPES_KEY = "scopes";

    /**
     * {@code PASSWORD_MAX_LENGTH} password max length
     */
    public static final int PASSWORD_MAX_LENGTH = 32;

    /**
     * {@code PASSWORD_MIN_LENGTH} password min length
     */
    public static final int PASSWORD_MIN_LENGTH = 8;

    /**
     * {@code PASSWORD_LENGTH_KEY} length key
     */
    public static final String PASSWORD_LENGTH_KEY = "length";

    /**
     * {@code PASSWORD_KEY} password key
     */
    public static final String PASSWORD_KEY = "password";

    /**
     * {@code tail} tail of the {@link #password}
     **/
    private final String tail;

    /**
     * {@code scopes} list of scopes where the {@link #password} can be used
     **/
    private final ArrayList<String> scopes;

    /**
     * {@code password} value
     **/
    private final String password;

    /**
     * {@code status} value
     **/
    private Status status;

    /**
     * Constructor to init {@link Password} object
     *
     * @param tail:     tail of the password
     * @param password: password value
     * @param status:   status value
     **/
    public Password(String tail, String password, Status status) {
        this(tail, new ArrayList<>(), password, status);
    }

    /**
     * Constructor to init {@link Password} object
     *
     * @param tail:     tail of the password
     * @param scopes:   list of scopes where the password can be used
     * @param password: password value
     * @param status:   status value
     **/
    public Password(String tail, ArrayList<String> scopes, String password, Status status) {
        this(null, tail, new ArrayList<>(), password, status);
    }

    /**
     * Constructor to init {@link Password} object
     *
     * @param token: session token value
     * @param tail:     tail of the password
     * @param password: password value
     * @param status:   status value
     **/
    public Password(String token, String tail, String password, Status status) {
        this(token, tail, new ArrayList<>(), password, status);
    }

    /**
     * Constructor to init {@link Password} object
     *
     * @param token: session token value
     * @param tail:     tail of the password
     * @param scopes:   list of scopes where the password can be used
     * @param password: password value
     * @param status:   status value
     **/
    public Password(String token, String tail, ArrayList<String> scopes, String password, Status status) {
        super(token);
        this.tail = tail;
        this.scopes = scopes;
        this.password = password;
        this.status = status;
    }

    /**
     * Method to get {@link #tail} instance <br>
     * Any params required
     *
     * @return {@link #tail} instance as {@link String}
     **/
    public String getTail() {
        return tail;
    }

    /**
     * Method to get {@link #scopes} instance <br>
     * Any params required
     *
     * @return {@link #scopes} instance as {@link Collection} of {@link String}
     **/
    public Collection<String> getScopes() {
        return scopes;
    }

    /**
     * Method to get {@link #scopes} instance <br>
     * Any params required
     *
     * @return {@link #scopes} instance as {@link Collection} of {@link String} alphabetically sorted
     **/
    public Collection<String> getScopesSorted() {
        ArrayList<String> sortedScopes = new ArrayList<>(scopes);
        Collections.sort(sortedScopes);
        return sortedScopes;
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
     * Method to get {@link #status} instance <br>
     * Any params required
     *
     * @return {@link #status} instance as {@link Status}
     **/
    public Status getStatus() {
        return status;
    }

    /**
     * Method to set the {@link #status} of the {@link #password} on {@link Status#ACTIVE}
     */
    public void activePassword() {
        status = Status.ACTIVE;
    }

    /**
     * Method to set the {@link #status} of the {@link #password} on {@link Status#DELETED}
     */
    public void deletePassword() {
        status = Status.DELETED;
    }

    /**
     * {@code Status} list of available statuses for a {@link Password}
     **/
    public enum Status {

        /**
         * {@code ACTIVE} status means the password is currently active and is possible to use it
         **/
        ACTIVE,

        /**
         * {@code DELETED} status means the password has been deleted and at the moment is not
         * possible use it, but can also be recovered
         **/
        DELETED

    }

}
