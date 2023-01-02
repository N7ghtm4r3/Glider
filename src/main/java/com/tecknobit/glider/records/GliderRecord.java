package com.tecknobit.glider.records;

import org.json.JSONObject;

public class GliderRecord {

    protected final String token;

    public GliderRecord(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
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
