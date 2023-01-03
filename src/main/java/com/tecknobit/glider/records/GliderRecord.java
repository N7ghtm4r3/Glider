package com.tecknobit.glider.records;

import org.json.JSONObject;

/**
 * The {@link GliderRecord} is class useful to create a {@code Glider}'s record and give the base structure
 * for all the records
 *
 * @author Tecknobit - N7ghtm4r3
 **/
public class GliderRecord {

    /**
     * {@code token} session token value
     **/
    protected final String token;

    /**
     * Constructor to init {@link GliderRecord} object
     *
     * @param token: session token value
     **/
    public GliderRecord(String token) {
        this.token = token;
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
